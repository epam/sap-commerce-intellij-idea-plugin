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
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.getPreferredFocusedComponent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.asSafely
import java.awt.BorderLayout
import java.awt.Dimension
import java.beans.PropertyChangeListener
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JPanel

class FlexibleSearchSplitEditor(private val flexibleSearchEditor: TextEditor, project: Project) : UserDataHolderBase(), FileEditor, TextEditor {

    private val flexibleSearchComponent: JComponent = createComponent(project)

    init {
        with(project.messageBus.connect(this)) {
            subscribe(MetaModelStateService.TOPIC, object : MetaModelChangeListener {
                override fun typeSystemChanged(globalMetaModel: TSGlobalMetaModel) {
                    refreshComponent()
                }
            })
        }
    }

    private fun isTsSystemInitialized(project: Project): Boolean {
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

    fun refreshComponent() {
        val project = editor.project
            ?.takeUnless { it.isDisposed }
            ?: return

        val splitter = flexibleSearchComponent.components.firstOrNull().asSafely<JBSplitter>() ?: return
        val isVisible = splitter.secondComponent.isVisible

        splitter.secondComponent = application.runReadAction<JComponent> {
            return@runReadAction buildPropertyForm(project)
        }
        splitter.secondComponent.isVisible = isVisible
    }

    fun toggleLayoutChange() {
        val splitter = flexibleSearchComponent.components.firstOrNull().asSafely<JBSplitter>() ?: return
        val parametersPanel = splitter.secondComponent
        parametersPanel.isVisible = !parametersPanel.isVisible

        flexibleSearchComponent.requestFocus()
        splitter.firstComponent.requestFocus()
    }

    fun isParameterPanelVisible(): Boolean = flexibleSearchComponent.components.firstOrNull()
        .asSafely<JBSplitter>()
        ?.secondComponent
        ?.isVisible
        ?: false

    private fun createComponent(project: Project): JComponent {
        val splitter = JBSplitter(false, 0.07f, 0.05f, 0.85f)
        splitter.splitterProportionKey = "SplitFileEditor.Proportion"
        splitter.firstComponent = flexibleSearchEditor.component

        if (project.isDisposed) {
            splitter.secondComponent = ScrollPaneFactory.createScrollPane(JPanel(), true).apply {
                //todo change to DSL initialization
                preferredSize = Dimension(600, 400)
            }
        } else {
            splitter.secondComponent = application.runReadAction<JComponent> {
                return@runReadAction buildPropertyForm(project)
            }
        }

        val result = JPanel(BorderLayout())
        result.add(splitter, BorderLayout.CENTER)

        return result
    }

    fun buildPropertyForm(project: Project): JComponent {
        if (project.isDisposed) {
            return ScrollPaneFactory.createScrollPane(JPanel(), true).apply {
                preferredSize = Dimension(600, 400)
            }
        }

        val isTsSystemInitialized = isTsSystemInitialized(project)
        var parametersPanel: DialogPanel?

        if (!isTsSystemInitialized) {
            parametersPanel = panel {
                row {
                    label("Initializing Type System, Please wait...")
                        .align(Align.CENTER)
                        .resizableColumn()
                }.resizableRow()
            }
        } else {
            val currentParameters = getUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS) ?: emptySet()
            val parameters = (PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java) }
                ?.map { bindParameter ->
                    val placeholder = bindParameter.text.removePrefix("?")
                    FlexibleSearchProperty(placeholder, currentParameters.find { it.name == placeholder }?.value ?: "")
                }
                ?.distinct()
                ?: emptySet())

            putUserData(KEY_FLEXIBLE_SEARCH_PARAMETERS, parameters)

            //extract to small methods: render headers, render no data panel, render data panel
            parametersPanel = panel {
                panel {
                    row {
                        val infoBanner = InlineBanner(
                            """
                        <html><body style='width: 100%'>
                        <p>This feature may be unstable. Use with caution.</p>
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

                panel {
                    row {
                        val infoBanner = InlineBanner(
                            """
                        <html><body style='width: 100%'>
                        <p>String parameters must be wrapped in single quotes: ''value''.</p>
                        </body></html>
                    """.trimIndent(),
                            EditorNotificationPanel.Status.Info
                        ).showCloseButton(false)

                        cell(infoBanner)
                            .align(Align.FILL)
                    }.topGap(TopGap.SMALL)
                }.customize(UnscaledGaps(16, 16, 16, 16))

                if (parameters.isEmpty()) {
                    row {
                        label("FlexibleSearch query doesn't have parameters")
                            .align(Align.CENTER)
                    }
                } else {
                    group("Parameters") {
                        parameters.forEach { property ->
                            row {
                                //todo limit the long name depends on width of the panel
                                label(property.name)
                                textField()
                                    .bindText(property::value)
                                    .onChanged { property.value = it.text }

                            }.layout(RowLayout.PARENT_GRID)
                        }
                    }
                }
            }
        }

//        return Dsl.scrollPanel(parametersPanel).apply {
//            isVisible = false
//        }
        return ScrollPaneFactory.createScrollPane(parametersPanel, true).apply {
            preferredSize = Dimension(600, 400)
            isVisible = false
        }
    }


    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        flexibleSearchEditor.addPropertyChangeListener(listener)
        flexibleSearchComponent.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        flexibleSearchEditor.removePropertyChangeListener(listener)
        flexibleSearchComponent.removePropertyChangeListener(listener)
    }

    override fun getPreferredFocusedComponent(): JComponent? = if (flexibleSearchEditor.component.isVisible) flexibleSearchEditor.preferredFocusedComponent
    else flexibleSearchComponent.getPreferredFocusedComponent()

    override fun getComponent(): JComponent = flexibleSearchComponent
    override fun getName(): String = "FlexibleSearch Split Editor"
    override fun setState(state: FileEditorState) = flexibleSearchEditor.setState(state)
    override fun isModified(): Boolean = flexibleSearchEditor.isModified
    override fun isValid(): Boolean = flexibleSearchEditor.isValid && flexibleSearchComponent.isValid
    override fun dispose() = Disposer.dispose(flexibleSearchEditor)
    override fun getEditor(): Editor = flexibleSearchEditor.editor
    override fun canNavigateTo(navigatable: Navigatable): Boolean = flexibleSearchEditor.canNavigateTo(navigatable)
    override fun navigateTo(navigatable: Navigatable) = flexibleSearchEditor.navigateTo(navigatable)
    override fun getFile(): VirtualFile? = editor.virtualFile

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L
    }
}

//create a factory method
data class FlexibleSearchProperty(
    var name: String,
    var value: String = "",
    var operand: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return name == (other as? FlexibleSearchProperty)?.name
    }
}
