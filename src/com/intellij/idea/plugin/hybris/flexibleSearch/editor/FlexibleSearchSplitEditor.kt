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

import com.intellij.idea.plugin.hybris.common.HybrisConstants.KEY_FLEXIBLE_SEARCH_PARAMETERS
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.system.meta.MetaModelChangeListener
import com.intellij.idea.plugin.hybris.system.meta.MetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.getPreferredFocusedComponent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.michaelbaranov.microba.calendar.DatePicker
import java.awt.BorderLayout
import java.awt.Dimension
import java.beans.PropertyChangeListener
import java.io.Serial
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel

class FlexibleSearchSplitEditor(private val textEditor: TextEditor, private val project: Project) : UserDataHolderBase(), FileEditor, TextEditor {

    private val splitter = OnePixelSplitter(false).apply {
        isShowDividerControls = true
        splitterProportionKey = "$javaClass.splitter"
        setHonorComponentsMinimumSize(true)

        firstComponent = textEditor.component
    }

    private val splitPanel = JPanel(BorderLayout()).apply {
        add(splitter, BorderLayout.CENTER)
    }

    init {
        with(project.messageBus.connect(this)) {
            subscribe(MetaModelStateService.TOPIC, object : MetaModelChangeListener {
                override fun typeSystemChanged(globalMetaModel: TSGlobalMetaModel) {
                    refreshParameterPanel()
                }
            })
        }
    }

    fun refreshParameterPanel() {
        if (project.isDisposed) return

        if (!isParametersPanelVisible()) return

        splitter.secondComponent = buildParametersPanel()
    }

    fun toggleLayout() {
        if (splitter.secondComponent == null) {
            splitter.secondComponent = buildParametersPanel()
        } else {
            splitter.secondComponent.apply { isVisible = !isVisible }
        }

        component.requestFocus()
        splitter.firstComponent.requestFocus()
    }

    fun isParametersPanelVisible(): Boolean = splitter.secondComponent?.isVisible ?: false

    fun buildParametersPanel(): JComponent? {
        if (project.isDisposed) return null

        if (!isTypeSystemInitialized()) return panel {
            row {
                label("Initializing Type System, please wait...")
                    .align(Align.CENTER)
                    .resizableColumn()
            }.resizableRow()
        }

        val parameters = collectParameters()

        //extract to small methods: render headers, render no data panel, render data panel
        return panel {
            panel {
                row {
                    val infoBanner = InlineBanner(
                        """
                        <html><body style='width: 100%'>
                        <p>This feature may be unstable. Use with caution. Submit issues or suggestions to project's GitHub repository.</p>
                        </body></html>
                    """.trimIndent(),
                        EditorNotificationPanel.Status.Warning
                    )

                    cell(infoBanner)
                        .align(Align.FILL)
                        .resizableColumn()
                }.topGap(TopGap.SMALL)
            }
                .customize(UnscaledGaps(16, 16, 16, 16))

            //todo extract from panel to show message vertical center aligned
            panel {
                if (parameters.isEmpty()) {
                    row {
                        label("FlexibleSearch query doesn't have parameters")
                            .align(Align.CENTER)
                    }
                } else {
                    group("Parameters") {
                        parameters.forEach { parameter ->
                            row {
                                //todo limit the long name depends on width of the panel
                                // TODO: migrate to proper property binding
                                when (parameter.type) {
                                    "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Integer",
                                    "float", "double", "byte", "short", "long", "int" -> intTextField()
                                        .label("${parameter.name}:")
                                        .align(AlignX.FILL)
                                        .text(parameter.value)
                                        .onChanged { parameter.value = it.text }

                                    "boolean",
                                    "java.lang.Boolean" -> checkBox(parameter.name)
                                        .align(AlignX.FILL)
                                        .selected(parameter.value == "1")
                                        .onChanged { parameter.value = if (it.isSelected) "1" else "0" }
                                        .also { parameter.value = (if (parameter.value == "1") "1" else "0") }

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
                                                        parameter.value = event.newValue
                                                            ?.asSafely<Date>()
                                                            ?.time
                                                            ?.toString() ?: ""
                                                    }
                                                }
                                                datePicker.addPropertyChangeListener(listener)
                                                Disposer.register(textEditor) {
                                                    datePicker.removePropertyChangeListener(listener)
                                                }
                                            }
                                        }

                                    else -> textField()
                                        .label("${parameter.name}:")
                                        .align(AlignX.FILL)
                                        .text(StringUtil.unquoteString(parameter.value, '\''))
                                        .onChanged { parameter.value = "'${it.text}'" }
                                }

                            }.layout(RowLayout.PARENT_GRID)
                        }
                    }
                }
            }
        }
            .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
            .let { Dsl.scrollPanel(it) }
            .apply {
                minimumSize = Dimension(minimumSize.width, 165)
            }
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.addPropertyChangeListener(listener)
        component.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.removePropertyChangeListener(listener)
        component.removePropertyChangeListener(listener)
    }

    override fun getPreferredFocusedComponent(): JComponent? = if (textEditor.component.isVisible) textEditor.preferredFocusedComponent
    else component.getPreferredFocusedComponent()

    override fun getComponent() = splitPanel
    override fun getName() = "FlexibleSearch Split Editor"
    override fun setState(state: FileEditorState) = textEditor.setState(state)
    override fun isModified() = textEditor.isModified
    override fun isValid() = textEditor.isValid && component.isValid
    override fun dispose() = Disposer.dispose(textEditor)
    override fun getEditor() = textEditor.editor
    override fun canNavigateTo(navigatable: Navigatable) = textEditor.canNavigateTo(navigatable)
    override fun navigateTo(navigatable: Navigatable) = textEditor.navigateTo(navigatable)
    override fun getFile(): VirtualFile? = editor.virtualFile

    private fun isTypeSystemInitialized(): Boolean {
        if (project.isDisposed) return false
        if (DumbService.isDumb(project)) return false

        try {
            val metaModelStateService = project.service<TSMetaModelStateService>()
            metaModelStateService.get()

            return metaModelStateService.initialized()
        } catch (_: Throwable) {
            return false
        }
    }

    private fun collectParameters(): Collection<FlexibleSearchParameter> {
        val currentParameters = getUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS) ?: emptySet()

        val parameters = application.runReadAction<Collection<FlexibleSearchParameter>> {
            PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java) }
                ?.map { FlexibleSearchParameter.of(it, currentParameters) }
                ?.distinctBy { it.name }
                ?: emptySet()
        }

        putUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS, parameters)
        return parameters
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L
    }
}

//create a factory method
data class FlexibleSearchParameter(
    val name: String,
    var value: String = "",
    val type: String? = null,
    val operand: IElementType? = null
) {
    companion object {
        fun of(bindParameter: FlexibleSearchBindParameter, currentParameters: Collection<FlexibleSearchParameter>): FlexibleSearchParameter {
            val parameter = bindParameter.text.removePrefix("?")
            val value = currentParameters.find { it.name == parameter }?.value ?: ""

            return FlexibleSearchParameter(parameter, value, bindParameter.itemType)
        }
    }
}
