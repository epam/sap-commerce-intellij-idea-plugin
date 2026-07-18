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

package sap.commerce.toolset.impex.lang.refactoring

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.ImpExDocumentIdReference
import sap.commerce.toolset.impex.psi.references.ImpExDocumentIdUsageReference

class ImpExRenamePsiElementProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement) = element is ImpExMacroNameDec
        || element is ImpExDocumentIdDec
        || element is ImpExDocumentIdUsage
        || (element is ImpExMacroUsageDec && !element.text.startsWith(ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER))
        || (element is ImpExValue && element.isDocIdValueCell())

    override fun findReferences(element: PsiElement, searchScope: SearchScope, searchInCommentsAndStrings: Boolean): Collection<PsiReference> {
        if (element is ImpExValue && element.isDocIdValueCell()) {
            val dec = element.getDocIdDecForValue() ?: return emptyList()
            val file = element.containingFile ?: return emptyList()
            return dec.findDocIdUsageReferences(element.text, file)
        }
        return findElements(element, element.text)
            .mapNotNull { it.reference }
            .toMutableList()
    }

    override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
        val namedElement = element as? ImpExPsiNamedElement ?: return

        namedElement.setName(newName)

        if (namedElement is ImpExValue) {
            /*
            # We have to rename the whole ImpExValue for all included in the rename action references in it. Example: NEW-underrename
            INSERT_UPDATE product ; &refId                 ; code[unique = true]
            VariantProduct        ; NEW-underrename         ; standard-net
            UPDATE BaseStore; uid[unique = true]; deliveryModes(&refId)
                            ; $storeUid         ; premiumtest,NEW-underrename,premium-gross,standard-gross,free-standard-shipping, NEW-underrename
             */
            usages
                .groupBy { it.smartPointer.element }
                .mapNotNull { (element, infos) ->
                    val value = element as? ImpExValue ?: return@mapNotNull null
                    var newText = value.text
                    infos
                        .mapNotNull { it.reference?.rangeInElement }
                        .sortedByDescending { it.startOffset }
                        .forEach { range ->
                            if (range.endOffset <= newText.length) {
                                newText = range.replace(newText, newName)
                            }
                        }
                    value to newText
                }
                .forEach { (value, newText) -> value.setName(newText) }

            usages
                .mapNotNull { it.reference }
                .sortedByDescending { it.absoluteRange.startOffset }
                .forEach { it.handleElementRename(newName) }
        } else {
            usages
                .mapNotNull { it.reference }
                .sortedByDescending { it.absoluteRange.startOffset }
                .forEach { it.handleElementRename(newName) }
        }

        listener?.elementRenamed(namedElement)
    }

    override fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val newRenames = allRenames
            .mapNotNull { (element, newName) ->
                findElements(element, element.text)
                    .map { it to newName }
            }
            .flatten()
            .toMap()

        allRenames.clear()
        allRenames.putAll(newRenames)
    }

    override fun findCollisions(element: PsiElement, newName: String, allRenames: MutableMap<out PsiElement, String>, result: MutableList<UsageInfo>) {
        allRenames
            .forEach { (element, newName) ->
                findElements(element, newName)
                    .forEach {
                        result.add(object : UnresolvableCollisionUsageInfo(it, element) {
                            override fun getDescription() = when (element.elementType) {
                                ImpExTypes.MACRO_NAME_DECLARATION,
                                ImpExTypes.MACRO_USAGE -> i18n("hybris.impex.refactoring.rename.existing.macroName.conflict", newName)

                                else -> i18n("hybris.impex.refactoring.rename.existing.conflict", newName)
                            }
                        })
                    }
            }
    }

    private fun findElements(renameElement: PsiElement, newText: String): Array<PsiElement> = renameElement.containingFile
        ?.let { file ->
            val filter = when (renameElement) {
                is ImpExMacroNameDec,
                is ImpExMacroUsageDec -> PsiElementFilter { element ->
                    if (element is ImpExMacroNameDec || element is ImpExMacroUsageDec) {
                        element.text == newText
                    } else {
                        false
                    }
                }

                is ImpExDocumentIdDec,
                is ImpExDocumentIdUsage -> PsiElementFilter { element ->
                    if (element is ImpExDocumentIdDec || element is ImpExDocumentIdUsage) {
                        element.text == newText
                    } else {
                        false
                    }
                }

                is ImpExValue -> {
                    val dec = renameElement.getDocIdDecForValue() ?: return emptyArray()
                    return dec.values[newText].orEmpty().toTypedArray()
                }

                else -> return@let null
            }

            PsiTreeUtil.collectElements(file, filter)
        }
        ?: emptyArray()

    private fun ImpExValue.isDocIdValueCell(): Boolean = this.getDocIdDecForValue() != null
    private fun ImpExValue.getDocIdDecForValue(): ImpExDocumentIdDec? = this
        .valueGroup?.fullHeaderParameter?.anyHeaderParameterName?.documentIdDec

    private fun ImpExDocumentIdDec.findDocIdUsageReferences(valueName: String, file: PsiFile): Collection<PsiReference> = PsiTreeUtil
        .collectElementsOfType(file, ImpExDocumentIdUsage::class.java)
        .filter { usage ->
            usage.reference?.asSafely<ImpExDocumentIdReference>()
                ?.multiResolve(false)
                ?.any { it.element == this } == true
        }
        .mapNotNull { it.parentOfType<ImpExFullHeaderParameter>() }
        .distinct()
        .flatMap { headerParam ->
            headerParam.valueGroups
                .mapNotNull { it.value }
                .flatMap { value ->
                    value.references
                        .filterIsInstance<ImpExDocumentIdUsageReference>()
                        .filter { it.value == valueName }
                }
        }
}