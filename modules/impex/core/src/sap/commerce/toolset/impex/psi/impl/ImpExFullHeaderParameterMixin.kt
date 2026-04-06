/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.*
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import java.io.Serial

abstract class ImpExFullHeaderParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExFullHeaderParameter {

    override fun getColumnNumber(): Int = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_COLUMN_NUMBER, {
        val columnNumber = ImpExPsiUtils.getColumnNumber(this)

        CachedValueProvider.Result.createSingleDependency(
            columnNumber,
            this
        )
    }, false)

    override fun getValueGroups(): List<ImpExValueGroup> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_GROUPS, {
        val valueGroups = this
            .headerLine
            ?.valueLines
            ?.mapNotNull { it.getValueGroup(this.columnNumber) }
            ?: emptyList()

        CachedValueProvider.Result.createSingleDependency(
            valueGroups,
            this
        )
    }, false)

    override fun isUnique() = this.getExpandedAttributes()
        .lastOrNull { it.anyAttributeName.textMatches(AttributeModifier.UNIQUE.modifierName) }
        ?.anyAttributeValue?.textMatches("true") ?: false

    override fun getAttribute(attributeModifier: AttributeModifier): ImpExAttribute? = this.modifiersList
        .flatMap { it.attributeList }
        .lastOrNull { it.anyAttributeName.textMatches(attributeModifier.modifierName) }

    override fun getAttributeValue(attributeModifier: AttributeModifier, defaultValue: String): String = getExpandedAttributes()
        .lastOrNull { it.anyAttributeName.textMatches(attributeModifier.modifierName) }
        ?.anyAttributeValue
        ?.childLeafs()
        ?.joinToString("") {
            if (it.elementType == ImpExTypes.MACRO_USAGE) it.parentOfType<ImpExMacroUsageDec>()
                ?.resolveValue(HashSet())
                ?: it.text
            else it.text
        }
        ?: defaultValue

    // In contrast to the "getAttribute" function, this function will expand any macros, create new temporary file and recreate FullHeaderParameter
    // due that, it will NOT BE PART of the original PsiTree and MUST NOT be accesses outside of this internal logic.
    private fun getExpandedAttributes(): List<ImpExAttribute> = CachedValuesManager.getManager(project)
        .getCachedValue(this, CACHE_KEY_EXPANDED_ATTRIBUTES, {
            val macroExpandedParameter = anyHeaderParameterName
                .takeIf { it.macroUsageDecList.isNotEmpty() }
                ?.children
                ?.joinToString("") {
                    when (it) {
                        is ImpExMacroUsageDec -> it.resolveValue(HashSet())
                        else -> it.text
                    }
                }
                ?.let { locallyExpandedParameter ->
                    val macros = buildString {
                        PsiTreeUtil.processElements(anyHeaderParameterName.containingFile) { element ->
                            if (element == this) return@processElements false
                            if (element is ImpExMacroDeclaration) this@buildString.appendLine(element.text)
                            return@processElements true
                        }
                    }
                    val locallyExpandedParameterWithModifiers = locallyExpandedParameter + this.modifiersList
                        .joinToString { it.text }
                    ImpExElementFactory.createFullHeaderParameter(project, macros, locallyExpandedParameterWithModifiers)
                }
                ?: this

            val expandedAttribute = macroExpandedParameter.modifiersList
                .flatMap { it.attributeList }

            CachedValueProvider.Result.createSingleDependency(
                expandedAttribute,
                this@ImpExFullHeaderParameterMixin
            )
        }, false)

    companion object {
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_GROUPS = Key.create<CachedValue<List<ImpExValueGroup>>>("SAP_CX_IMPEX_VALUE_GROUPS")
        val CACHE_KEY_EXPANDED_ATTRIBUTES = Key.create<CachedValue<List<ImpExAttribute>>>("SAP_CX_IMPEX_EXPANDED_ATTRIBUTE")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}