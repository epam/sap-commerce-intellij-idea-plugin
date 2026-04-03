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

package sap.commerce.toolset.impex.lang.findUsages

import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorLocation
import com.intellij.psi.PsiReference
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsagePresentation
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.psi.ImpExPsiNamedElement

class ImpExPsiElementUsage<T : ImpExPsiNamedElement>(
    private val myElement: T,
    psiReference: PsiReference,
) : PsiElementUsage {

    private val usageAdapter: UsagePresentation = UsageInfo2UsageAdapter(UsageInfo(psiReference))

    override fun getElement(): T = myElement
    override fun isNonCodeUsage(): Boolean = false
    override fun getPresentation(): UsagePresentation = usageAdapter
    override fun isValid(): Boolean = myElement.isValid
    override fun isReadOnly(): Boolean = !myElement.isWritable
    override fun selectInEditor() = Unit
    override fun highlightInEditor() = Unit

    override fun getLocation(): FileEditorLocation? = element.containingFile.virtualFile
        ?.let { file -> FileEditorManager.getInstance(this.myElement.project).getSelectedEditor(file) }
        ?.asSafely<TextEditor>()
        ?.let { TextEditorLocation(0, it) }
}
