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

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.UIBundle
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.michaelbaranov.microba.calendar.DatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.beans.PropertyChangeListener
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.LayoutFocusTraversalPolicy

@Service(Service.Level.PROJECT)
class FlexibleSearchInEditorParametersView(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun renderParameters(fileEditor: FlexibleSearchSplitEditor) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            fileEditor.queryParametersDisposable?.let { Disposer.dispose(it) }

            val panel = if (!isTypeSystemInitialized()) renderTypeSystemInitializationPanel()
            else {
                val queryParameters = collectQueryParameters(fileEditor)
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
        queryParameters: Map<String, FlexibleSearchQueryParameter>,
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
        queryParameters: Map<String, FlexibleSearchQueryParameter>,
        fileEditor: FlexibleSearchSplitEditor,
        parentDisposable: Disposable
    ) = panel {
        group("Parameters") {
            queryParameters.forEach { name, parameter ->
                row {
                    when (parameter.type) {
                        Byte::class -> numberTextField(parameter, fileEditor, "-128", "127", "byte")
                        { it.toByteOrNull() == null }

                        Short::class -> numberTextField(parameter, fileEditor, "-32,768", "32,767", "short")
                        { it.toShortOrNull() == null }

                        Int::class -> numberTextField(parameter, fileEditor, "-2,147,483,648", "2,147,483,647", "int")
                        { it.toIntOrNull() == null }

                        Long::class -> numberTextField(parameter, fileEditor, "-9,223,372,036,854,775,808", "9,223,372,036,854,775,807", "long")
                        { it.toLongOrNull() == null }

                        Float::class -> numberTextField(parameter, fileEditor, "1.4E-45F", "3.4E+38F", "float")
                        { it.toFloatOrNull() == null }

                        Double::class -> numberTextField(parameter, fileEditor, "4.9E-324", "1.8E+308", "double")
                        { it.toDoubleOrNull() == null }

                        Boolean::class -> checkBox(parameter.displayName)
                            .align(AlignX.FILL)
                            .selected(parameter.sqlValue == "1")
                            .onChanged { applyValue(fileEditor, parameter, it.isSelected) }

                        Date::class -> cell(
                            DatePicker(
                                parameter.rawValue?.asSafely<Date>(),
                                SimpleDateFormat(FlexibleSearchQueryParameter.DATE_FORMAT)
                            )
                        )
                            .label("${parameter.displayName}:")
                            .align(Align.FILL).apply {
                                component.also { datePicker ->
                                    val listener = PropertyChangeListener { event ->
                                        if (event.propertyName == "date") {
                                            applyValue(fileEditor, parameter, event.newValue?.asSafely<Date>())
                                        }
                                    }
                                    datePicker.addPropertyChangeListener(listener)
                                    Disposer.register(parentDisposable) {
                                        datePicker.removePropertyChangeListener(listener)
                                    }
                                }
                            }

                        String::class -> when {
                            parameter.operand == FlexibleSearchTypes.IN_EXPRESSION -> multivalueTextArea()
                            else -> textField()
                        }
                            .label("${parameter.displayName}:")
                            .align(AlignX.FILL)
                            .text(parameter.rawValue?.asSafely<String>() ?: "")
                            .onChanged { applyValue(fileEditor, parameter, it.text) }

                        else -> when {
                            parameter.operand == FlexibleSearchTypes.IN_EXPRESSION -> multivalueTextArea()
                            else -> textField()
                        }
                            .label("${parameter.displayName}:")
                            .align(AlignX.FILL)
                            .text(parameter.sqlValue)
                            .onChanged { applyValue(fileEditor, parameter, it.text) }
                    }

                }.layout(RowLayout.PARENT_GRID)
            }
        }
    }

    private fun Row.numberTextField(
        parameter: FlexibleSearchQueryParameter,
        fileEditor: FlexibleSearchSplitEditor,
        from: String, to: String,
        numberType: String,
        validation: (String) -> Boolean
    ) = when {
        parameter.operand == FlexibleSearchTypes.IN_EXPRESSION -> multivalueTextArea()
        else -> textField().validationOnInput {
            if (validation.invoke(it.text)) error(UIBundle.message("please.enter.a.number.from.0.to.1", from, "$to ($numberType)"))
            else null
        }
    }
        .label("${parameter.displayName}:")
        .align(AlignX.FILL)
        .text(parameter.rawValue?.asSafely<String>() ?: "")
        .onChanged { applyValue(fileEditor, parameter, it.text) }

    private fun Row.multivalueTextArea(): Cell<JBTextArea> = textArea()
        .rows(3)
        .comment("Use new line as a value separator.")

    private suspend fun collectQueryParameters(fileEditor: FlexibleSearchSplitEditor): Map<String, FlexibleSearchQueryParameter> {
        val currentQueryParameters = fileEditor.queryParameters
            ?: emptyMap()

        return readAction {
            PsiDocumentManager.getInstance(project).getPsiFile(fileEditor.editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java) }
                ?.map { FlexibleSearchQueryParameter.of(it, currentQueryParameters) }
                ?.distinctBy { it.name }
                ?.associateBy { it.name }
                ?: emptyMap()
        }
            .also {
                fileEditor.queryParameters = it
            }
    }

    private fun applyValue(fileEditor: FlexibleSearchSplitEditor, parameter: FlexibleSearchQueryParameter, newRawValue: Any?) {
        val originalRawValue = parameter.rawValue

        parameter.rawValue = newRawValue

        if (originalRawValue != parameter.rawValue) {
            fileEditor.reparseTextEditor()
        }
    }

    private fun isTypeSystemInitialized(): Boolean = !project.isDisposed
        && !DumbService.isDumb(project)
        && TSMetaModelStateService.getInstance(project).initialized()

    companion object {
        fun getInstance(project: Project): FlexibleSearchInEditorParametersView = project.service()
    }
}