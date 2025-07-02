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

package com.intellij.idea.plugin.hybris.system.java.codeInsight.hints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.ide.DataManager
import com.intellij.idea.plugin.hybris.common.HybrisConstants.KEY_FLEXIBLE_SEARCH_PARAMETERS
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class FlexibleSearchParameterInlayHintProvider : InlayHintsProvider {

    private val collector by lazy {
        object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                if (!element.isValid || element.project.isDefault) return
                if (element !is FlexibleSearchBindParameter) return
                FileEditorManager.getInstance(element.project)
                    .getSelectedEditor(element.containingFile.virtualFile)
                    .asSafely<FlexibleSearchSplitEditor>()
                    ?.getUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS)
                    ?.find { it.name == element.text.removePrefix("?") && it.value.isNotBlank() }
                    ?.let {
                        sink.addPresentation(
                            position = InlineInlayPosition(element.textRange.endOffset, true),
                            payloads = null,
                            tooltip = null,
                            hintFormat = HintFormat(HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor, HintMarginPadding.MarginAndSmallerPadding),
                        ) {
                            text("=${it.value}")
                        }
                    }
            }
        }
    }

    override fun createCollector(file: PsiFile, editor: Editor) =
        if (ProjectSettingsComponent.getInstance(file.project).isHybrisProject()) collector
        else null
}