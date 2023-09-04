package com.intellij.idea.plugin.hybris.impex.inspection.analyzer

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

fun notKeyAttributesList(fullParametersList: List<ImpexFullHeaderParameter>) = fullParametersList.filterNot { keyAttrPredicate(it) }

fun keyAttributesList(fullParametersList: List<ImpexFullHeaderParameter>) = fullParametersList.filter { keyAttrPredicate(it) }

fun fullParametersList(headerLines: List<ImpexHeaderLine>) = headerLines.flatMap { it.fullHeaderParameterList }

fun keyAttrsName(it: ImpexHeaderLine) = it.fullHeaderParameterList.filter { keyAttrPredicate(it) }.map { it.text }

fun keyAttrPredicate(param: ImpexFullHeaderParameter) = param.modifiersList
    .flatMap { it.attributeList }
    .find { it.anyAttributeName.text == AttributeModifier.UNIQUE.modifierName && it.anyAttributeValue?.text == "true" } != null

fun intersection(a: ByteArray, b: ByteArray) = a.filterIndexed { index, i -> b[index] != 0.toByte() && b[index] == i }.isNotEmpty()

fun createDataTable(dataMap: Map<String, List<PsiElement>>, distinctCommonAttrsNames: List<String>, notKeyAttrsList: List<ImpexFullHeaderParameter>): DataTable {
    val countKeyAttrs = dataMap.entries.size
    val countRows = dataMap.values.first().size

    val keyRows = createRows(countRows, countKeyAttrs, dataMap)

    return DataTable(keyRows, distinctCommonAttrsNames, notKeyAttrsList)
}

fun createRows(countRows: Int, countKeyAttrs: Int, dataMap: Map<String, List<PsiElement>>): MutableList<Key> {
    val keyRows = mutableListOf<Key>()
    for (i in 0 until countRows) {
        val k = mutableListOf<PsiElement>()
        for (y in 0 until countKeyAttrs) {
            val entry = dataMap.entries.toList()[y]
            if ((entry.value as List<*>).isNotEmpty() && entry.value.size > i) {
                k.add(entry.value[i])
            }
        }
        keyRows.add(Key(k))
    }
    return keyRows
}

class DataTable(private val keyRows: List<Key>, private val attrs: List<String>, private val attrsValues: List<ImpexFullHeaderParameter>) {

    private val rows = mutableListOf<Row>()
    private val errorBag = mutableSetOf<PsiElement>()
    private val warningBag = mutableSetOf<PsiElement>()

    fun analyze(problemsHolder: ProblemsHolder) {

        analyzeProblems()
        collectProblems()
        showFoundProblems(problemsHolder)
    }

    private fun collectProblems() {
        val groupBy = rows.groupBy { it.key.toString() }
        groupBy.forEach { _, rows: List<Row> ->
            rows.forEach { row1 ->
                rows.forEach { row2 ->
                    if (row1.columns != row2.columns && intersection(row1.columns, row2.columns)) {
                        row1.columns.forEachIndexed { idx, az ->
                            val row1Element = row1.valueGroup[idx]
                            val row2Element = row2.valueGroup[idx]

                            if ((row1Element != null && row1Element.text.replace(";", "").isNotBlank()) &&
                                (row2Element != null && row2Element.text.replace(";", "").isNotBlank())
                            ) {
                                if (az == 1.toByte()) {
                                    if (!errorBag.contains(row2Element) && !warningBag.contains(row2Element))
                                        warningBag.add(row2Element)

                                    if (!errorBag.contains(row1Element) && !warningBag.contains(row1Element))
                                        errorBag.add(row1Element)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showFoundProblems(problemsHolder: ProblemsHolder) {

        warningBag.forEach {
            problemsHolder.registerProblem(
                it, "This value will override the value above",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }
        errorBag.forEach {
            problemsHolder.registerProblem(
                it, "This value is overridden by a value below",
                ProblemHighlightType.GENERIC_ERROR
            )
        }
    }

    private fun analyzeProblems() {
        keyRows.forEach { keyValue ->
            val bitSet = ByteArray(attrs.size)

            val row = Row(keyValue, bitSet, arrayOfNulls(attrs.size))
            attrs.forEach { av ->
                val valueGroups =
                    attrsValues.filter { it.text == av }
                        .filter { hasNoAppendModeModifier(it) }
                        .flatMap { ImpexPsiUtils.getColumnForHeader(it) }
                        .filter { it.value != null }
                        .filter {
                            PsiTreeUtilExt.getLeafsOfAnyElementType(
                                it.value!!,
                                ImpexTypes.COLLECTION_APPEND_PREFIX,
                                ImpexTypes.COLLECTION_REMOVE_PREFIX,
                                ImpexTypes.COLLECTION_MERGE_PREFIX
                            ).isEmpty()
                        }
                        .filter {
                            val commonContext = PsiTreeUtil.findCommonContext(keyValue.keys.first(), it)
                            commonContext != null && commonContext !is ImpexFile
                        }

                if (valueGroups.isNotEmpty()) {
                    valueGroups.forEach { valueGroup ->
                        if (valueGroup == null) {
                            val indexOf = attrs.indexOfFirst { s -> s == av }
                            if (indexOf > -1) {
                                bitSet[indexOf] = 0
                                row.valueGroup[indexOf] = null
                            }
                        } else {
                            val headerForValueGroup = ImpexPsiUtils.getHeaderForValueGroup(valueGroup as? ImpexValueGroup)
                            if (headerForValueGroup is ImpexFullHeaderParameter) {
                                val indexOf = attrs.indexOfFirst { headerForValueGroup.text == it }
                                if (indexOf > -1) {
                                    bitSet[indexOf] = 1
                                    row.valueGroup[indexOf] = valueGroup
                                }
                            }
                        }
                    }
                }
            }
            rows.add(row)
        }
    }

    private fun hasNoAppendModeModifier(headerParameter: ImpexFullHeaderParameter) = !headerParameter.modifiersList
        .flatMap { it.attributeList }
        .any {
            it.anyAttributeName.text == AttributeModifier.LANG.modifierName
                || (it.anyAttributeName.text == AttributeModifier.MODE.modifierName && it.anyAttributeValue?.text == "append")
        }

}

class Row(val key: Key, val columns: ByteArray, val valueGroup: Array<PsiElement?>)

class Key(val keys: List<PsiElement>) {
    override fun toString(): String = keys.joinToString { "|" + it.text }
}
