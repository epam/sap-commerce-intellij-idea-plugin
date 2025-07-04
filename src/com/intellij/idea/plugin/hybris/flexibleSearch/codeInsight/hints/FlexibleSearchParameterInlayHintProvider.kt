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

package com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.hints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely

class FlexibleSearchParameterInlayHintProvider : InlayHintsProvider {

    private val collector by lazy {
        object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                if (!element.isValid || element.project.isDefault) return
                if (element !is FlexibleSearchBindParameter) return
                FileEditorManager.getInstance(element.project)
                    .getSelectedEditor(element.containingFile.virtualFile)
                    .asSafely<FlexibleSearchSplitEditor>()
                    ?.getParameters()
                    ?.find { it.name == element.value && it.presentationValue.isNotBlank() }
                    ?.let {
                        sink.addPresentation(
                            position = InlineInlayPosition(element.textRange.endOffset, true),
                            payloads = null,
                            tooltip = "SQL value: ${it.value}",
                            hintFormat = HintFormat(HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor, HintMarginPadding.MarginAndSmallerPadding),
                        ) {
                            text("= ${it.presentationValue}")
                        }
                    }
            }
        }
    }

    override fun createCollector(file: PsiFile, editor: Editor) = if (ProjectSettingsComponent.Companion.getInstance(file.project).isHybrisProject()) collector
    else null
}