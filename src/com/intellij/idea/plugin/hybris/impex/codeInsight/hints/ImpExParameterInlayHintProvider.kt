/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.codeInsight.hints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.idea.plugin.hybris.impex.editor.ImpExSplitEditor
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely

class ImpExParameterInlayHintProvider : InlayHintsProvider {

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        if (file.isNotHybrisProject) return null

        return FileEditorManager.getInstance(file.project).allEditors
            .filterIsInstance<ImpExSplitEditor>()
            .find { it.editor == editor }
            ?.let { ImpExInlayHintsCollector(it) }
    }

    private class ImpExInlayHintsCollector(private val splitEditor: ImpExSplitEditor) : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (!element.isValid || element.project.isDefault) return
            if (!splitEditor.inEditorParameters) return

            element
                .asSafely<ImpexMacroDeclaration>()
                ?.let { splitEditor.virtualParameter(it) }
                ?.let {
                    sink.addPresentation(
                        position = InlineInlayPosition(element.textRange.endOffset, true),
                        payloads = null,
                        hintFormat = HintFormat(HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor, HintMarginPadding.MarginAndSmallerPadding),
                    ) {
                        text("= ${it.presentationValue}")
                    }
                }
        }
    }
}