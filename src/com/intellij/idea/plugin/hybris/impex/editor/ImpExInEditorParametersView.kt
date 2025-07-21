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

package com.intellij.idea.plugin.hybris.impex.editor

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import javax.swing.LayoutFocusTraversalPolicy

@Service(Service.Level.PROJECT)
class ImpExInEditorParametersView(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun renderParameters(fileEditor: ImpExSplitEditor) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            fileEditor.virtualParametersDisposable?.let { Disposer.dispose(it) }

            val queryParameters = collectQueryParameters(fileEditor)
            fileEditor.virtualParameters = collectQueryParameters(fileEditor)
            val panel = renderParametersPanel(queryParameters, fileEditor)

            edtWriteAction {
                fileEditor.inEditorParametersView = panel
            }
        }
    }

    private fun renderParametersPanel(
        queryParameters: Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter>,
        fileEditor: ImpExSplitEditor,
    ): DialogPanel {
        val parentDisposable = Disposer.newDisposable().apply {
            fileEditor.virtualParametersDisposable = this
            Disposer.register(fileEditor.textEditor, this)
        }

        return panel {
            notificationPanel()

            if (queryParameters.isEmpty()) {
                notResultsPanel()
            } else {
                parametersPanel(queryParameters, fileEditor)
            }
        }
            .apply {
                border = JBUI.Borders.empty(5, 16, 10, 16)
                registerValidators(parentDisposable)
            }
            .let { Dsl.scrollPanel(it) }
            .apply {
                minimumSize = Dimension(minimumSize.width, 165)
                focusTraversalPolicy = LayoutFocusTraversalPolicy()
                isFocusCycleRoot = true
            }
    }

    private fun Panel.notificationPanel() = panel {
        row {
            cell(
                InlineBanner(
                    """
                            <html><body style='width: 100%'>
                            <p>This feature is experimental and may be unstable. Use with caution.</p>
                            <p>Submit issues or suggestions to project's GitHub <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/new">repository</a>.</p>
                            </body></html>
                        """.trimIndent(),
                    EditorNotificationPanel.Status.Promo
                )
            )
                .align(Align.FILL)
                .resizableColumn()
        }.topGap(TopGap.SMALL)
    }.customize(UnscaledGaps(16, 16, 16, 16))

    private fun Panel.notResultsPanel() = panel {
        row {
            cell(
                InlineBanner(
                    "<p style='width: 100%'>Polyglot Query doesn't have parameters</p>",
                    EditorNotificationPanel.Status.Warning
                ).showCloseButton(false)
            )
                .align(Align.FILL)
                .resizableColumn()
        }.topGap(TopGap.SMALL)
    }.customize(UnscaledGaps(16, 16, 16, 16))

    private fun Panel.parametersPanel(
        queryParameters: Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter>,
        fileEditor: ImpExSplitEditor
    ) = panel {
        group("Macro Declarations") {
            queryParameters.values
                .forEach { parameter ->
                    row {
                        textField()
                            .label("${parameter.displayName}:")
                            .align(AlignX.FILL)
                            .text(parameter.rawValue ?: "")
                            .onChanged { applyValue(fileEditor, parameter, it.text) }
                    }.layout(RowLayout.PARENT_GRID)
                }
        }
    }

    private suspend fun collectQueryParameters(fileEditor: ImpExSplitEditor): Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter> {
        val currentParameters = fileEditor.virtualParameters
            ?: emptyMap()

        return readAction {
            PsiDocumentManager.getInstance(project).getPsiFile(fileEditor.editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, ImpexMacroDeclaration::class.java) }
                ?.associate {
                    val pointer = SmartPointerManager.createPointer(it)
                    val virtualParameter = currentParameters[pointer]
                        ?: ImpExVirtualParameter.of(it)

                    pointer to virtualParameter
                }
                ?: emptyMap()
        }
            .also {
                fileEditor.virtualParameters = it
            }
    }

    private fun applyValue(fileEditor: ImpExSplitEditor, parameter: ImpExVirtualParameter, newRawValue: String) {
        val originalRawValue = parameter.rawValue

        parameter.rawValue = newRawValue

        if (originalRawValue != parameter.rawValue) {
            fileEditor.reparseTextEditor()
        }
    }

    companion object {
        fun getInstance(project: Project): ImpExInEditorParametersView = project.service()
    }
}
