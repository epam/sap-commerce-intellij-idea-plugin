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

import com.intellij.database.dialects.base.findChild
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.editor.ImpExSplitEditor
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExMacroNameDec
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.util.getKey
import sap.commerce.toolset.impex.psi.util.setName
import java.io.Serial

abstract class ImpExMacroNameDecMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExMacroNameDec {

    override fun setName(newName: String): PsiElement = setName(this, newName)
    override fun getNameIdentifier() = this
    override fun getName() = getKey(node)
    override fun toString() = text
        ?: super.toString()

    override fun resolveValue(evaluatedMacroUsages: MutableSet<ImpExMacroUsageDec?>): String = CachedValuesManager.getManager(project).getCachedValue(
        this,
        Key.create("SAP_CX_IMPEX_RESOLVED_VALUE_" + evaluatedMacroUsages.size),
        {
            val resolvedValue = resolveVirtualParameter()
                ?: this.parent.findChild(ImpExTypes.MACRO_VALUES_DEC)
                    ?.childLeafs()
                    ?.map { psi ->
                        psi
                            .takeIf { it.elementType == ImpExTypes.MACRO_USAGE }
                            ?.parentOfType<ImpExMacroUsageDec>()
                            ?.takeUnless { evaluatedMacroUsages.contains(it) }
                            ?.let { macroUsage ->
                                evaluatedMacroUsages.add(macroUsage)

                                val ref = macroUsage.reference ?: return@let null
                                val resolveValue = macroUsage.resolveValue(evaluatedMacroUsages)

                                resolveValue + ref.element.text.substringAfter(ref.canonicalText, "")
                            }
                            ?: psi.text
                    }
                    ?.joinToString("")
                    ?.trim()
                ?: ""

            CachedValueProvider.Result.createSingleDependency(
                resolvedValue,
                this
            )
        }, false
    )

    private fun resolveVirtualParameter(): String? {
        val macroDeclaration = parentOfType<ImpExMacroDeclaration>()
            ?: return null
        val vf = containingFile.virtualFile
            ?: return null
        return FileEditorManager.getInstance(project).getSelectedEditor(vf)
            ?.asSafely<ImpExSplitEditor>()
            ?.virtualParameter(macroDeclaration)
            ?.rawValue
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 1984651966859085911L
    }
}
