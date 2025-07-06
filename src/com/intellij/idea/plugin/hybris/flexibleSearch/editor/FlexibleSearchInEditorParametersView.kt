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

package com.intellij.idea.plugin.hybris.flexibleSearch.editor

import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor.Companion.KEY_FLEXIBLE_SEARCH_PARAMETERS
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.michaelbaranov.microba.calendar.DatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.beans.PropertyChangeListener
import java.text.SimpleDateFormat
import java.util.*

object FlexibleSearchInEditorParametersView {

    fun renderParameters(project: Project, fileEditor: FlexibleSearchSplitEditor) {
        CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            fileEditor.queryParametersDisposable?.let { Disposer.dispose(it) }

            val panel = if (!isTypeSystemInitialized(project)) renderTypeSystemInitializationPanel()
            else {
                val queryParameters = readAction { collectParameters(project, fileEditor) }
                renderParametersPanel(queryParameters, fileEditor)
            }

            edtWriteAction {
                fileEditor.inEditorParametersView = panel
            }
        }
    }

    private fun renderTypeSystemInitializationPanel() = panel {
        row {
            label("Initializing Type System, please wait...")
                .align(Align.CENTER)
                .resizableColumn()
        }.resizableRow()
    }

    private fun renderParametersPanel(
        queryParameters: Collection<FlexibleSearchQueryParameter>,
        fileEditor: FlexibleSearchSplitEditor,
    ): DialogPanel {
        val parentDisposable = Disposer.newDisposable().apply {
            fileEditor.queryParametersDisposable = this
            Disposer.register(fileEditor.textEditor, this)
        }

        return panel {
            notificationPanel()

            if (queryParameters.isEmpty()) {
                notResultsPanel()
            } else {
                parametersPanel(queryParameters, fileEditor, parentDisposable)
            }
        }
            .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
            .let { Dsl.scrollPanel(it) }
            .apply {
                minimumSize = Dimension(minimumSize.width, 165)
            }
    }

    private fun Panel.notificationPanel() = panel {
        row {
            cell(
                InlineBanner(
                    """
                            <html><body style='width: 100%'>
                            <p>This feature may be unstable. Use with caution.</p>
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
                    "<p style='width: 100%'>FlexibleSearch query doesn't have parameters</p>",
                    EditorNotificationPanel.Status.Warning
                ).showCloseButton(false)
            )
                .align(Align.FILL)
                .resizableColumn()
        }.topGap(TopGap.SMALL)
    }.customize(UnscaledGaps(16, 16, 16, 16))

    private fun Panel.parametersPanel(
        queryParameters: Collection<FlexibleSearchQueryParameter>,
        fileEditor: FlexibleSearchSplitEditor,
        parentDisposable: Disposable
    ) = panel {
        group("Parameters") {
            queryParameters.forEach { parameter ->
                row {
                    //todo limit the long name depends on width of the panel
                    // TODO: migrate to proper property binding
                    when (parameter.type) {
                        "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Integer",
                        "float", "double", "byte", "short", "long", "int" -> intTextField()
                            .label("${parameter.name}:")
                            .align(AlignX.FILL)
                            .text(parameter.value)
                            .onChanged { applyValue(fileEditor, parameter, it.text) { it.text } }

                        "boolean",
                        "java.lang.Boolean" -> checkBox(parameter.name)
                            .align(AlignX.FILL)
                            .selected(parameter.value == "1")
                            .onChanged {
                                val presentationValue = if (it.isSelected) "true" else "false"
                                applyValue(fileEditor, parameter, presentationValue) { if (it.isSelected) "1" else "0" }
                            }
                            .also {
                                parameter.value = (if (parameter.value == "1") "1" else "0")
                            }

                        "java.util.Date" -> cell(
                            DatePicker(
                                parameter.value.toLongOrNull()?.let { Date(it) },
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                            )
                        )
                            .label("${parameter.name}:")
                            .align(Align.FILL).apply {
                                component.also { datePicker ->
                                    val listener = PropertyChangeListener { event ->
                                        if (event.propertyName == "date") {
                                            val newValue = event.newValue?.asSafely<Date>()
                                            val presentationValue = newValue?.let { date -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date) } ?: ""

                                            applyValue(fileEditor, parameter, presentationValue) {
                                                newValue?.time?.toString() ?: ""
                                            }
                                        }
                                    }
                                    datePicker.addPropertyChangeListener(listener)
                                    Disposer.register(parentDisposable) {
                                        datePicker.removePropertyChangeListener(listener)
                                    }
                                }
                            }

                        "String",
                        "java.lang.String",
                        "localized:java.lang.String" -> textField()
                            .label("${parameter.name}:")
                            .align(AlignX.FILL)
                            .text(StringUtil.unquoteString(parameter.value, '\''))
                            .onChanged { applyValue(fileEditor, parameter, "'${it.text}'") { "'${it.text}'" } }

                        else -> textField()
                            .label("${parameter.name}:")
                            .align(AlignX.FILL)
                            .text(parameter.value)
                            .onChanged { applyValue(fileEditor, parameter, it.text) { "${it.text}" } }
                    }

                }.layout(RowLayout.PARENT_GRID)
            }
        }
    }

    private fun collectParameters(project: Project, fileEditor: FlexibleSearchSplitEditor): Collection<FlexibleSearchQueryParameter> {
        val currentParameters = fileEditor.queryParameters ?: emptySet()

        val parameters = application.runReadAction<Collection<FlexibleSearchQueryParameter>> {
            PsiDocumentManager.getInstance(project).getPsiFile(fileEditor.editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java) }
                ?.map { FlexibleSearchQueryParameter.of(it, currentParameters) }
                ?.distinctBy { it.name }
                ?: emptySet()
        }

        fileEditor.putUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS, parameters)
        return parameters
    }

    private fun applyValue(fileEditor: FlexibleSearchSplitEditor, parameter: FlexibleSearchQueryParameter, presentationValue: String, newValueProvider: () -> String) {
        val originalValue = parameter.value
        parameter.presentationValue = presentationValue
        parameter.value = newValueProvider.invoke()

        if (originalValue != parameter.value) {
            fileEditor.reparseTextEditor()
        }
    }

    private fun isTypeSystemInitialized(project: Project): Boolean = !project.isDisposed
        && !DumbService.isDumb(project)
        && project.service<TSMetaModelStateService>().initialized()
}