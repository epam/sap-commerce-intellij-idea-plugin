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

import com.intellij.idea.plugin.hybris.common.HybrisConstants.FLEXIBLE_SEARCH_PROPERTIES_KEY
import com.intellij.idea.plugin.hybris.flexibleSearch.listeners.FlexibleSearchSplitEditorListener
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.system.meta.MetaModelChangeListener
import com.intellij.idea.plugin.hybris.system.meta.MetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSViewSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
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
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.messages.Topic
import java.awt.BorderLayout
import java.awt.Dimension
import java.beans.PropertyChangeListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FlexibleSearchSplitEditor : UserDataHolderBase, FileEditor, TextEditor {

    private val flexibleSearchEditor: TextEditor
    private val flexibleSearchComponent: JComponent

    constructor(e: TextEditor, project: Project) : super() {
        flexibleSearchEditor = e
        flexibleSearchComponent = createComponent(project)

        flexibleSearchEditor.editor.document.addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                if (!project.isDisposed) {
                    project.messageBus.syncPublisher(TOPIC_EDITOR_CHANGED).editorChanged(event.document)
                }
            }
        })

        with(project.messageBus.connect(this)) {
            subscribe(TOPIC_EDITOR_CHANGED, object : FlexibleSearchSplitEditorListener {
                override fun editorChanged(document: Document) {
                    if (flexibleSearchEditor.editor.document == document) {
                        PsiDocumentManager.getInstance(project).commitDocument(editor.document)

                        refreshComponent(project)
                    }
                }
            })

            subscribe(TSViewSettings.TOPIC, object : TSViewSettings.Listener {
                override fun settingsChanged(changeType: TSViewSettings.ChangeType) {
                    refreshComponent(project)
                }
            })

            subscribe(MetaModelStateService.TOPIC, object : MetaModelChangeListener {
                override fun typeSystemChanged(globalMetaModel: TSGlobalMetaModel) {
                    refreshComponent(project)
                }
            })
        }
    }

    private fun isTsSystemInitialized(project: Project): Boolean {
        if (project.isDisposed) {
            return false
        }

        val metaModelStateService = project.service<TSMetaModelStateService>()

        try {
            metaModelStateService.get()

            return when {
                DumbService.isDumb(project) -> false
                !metaModelStateService.initialized() -> false

                else -> true
            }
        } catch (_: Throwable) {
            return false
        }
    }

    private fun refreshComponent(project: Project) {
        if (project.isDisposed) {
            return
        }

        val splitter = flexibleSearchComponent.components[0] as JBSplitter
        val isVisible = splitter.secondComponent.isVisible

        splitter.secondComponent = application.runReadAction<JScrollPane> {
            return@runReadAction buildPropertyForm(project)
        }
        splitter.secondComponent.isVisible = isVisible

    }

    fun triggerLayoutChange(showPanel: Boolean) {
        val splitter = flexibleSearchComponent.components[0] as JBSplitter
        val parametersPanel = splitter.secondComponent
        parametersPanel.isVisible = showPanel

        flexibleSearchComponent.requestFocus()
        splitter.firstComponent.requestFocus()
    }

    fun isParameterPanelVisible(): Boolean {
        val splitter = flexibleSearchComponent.components[0] as JBSplitter
        return splitter.secondComponent.isVisible
    }

    private fun createComponent(project: Project): JComponent {
        val splitter = JBSplitter(false, 0.07f, 0.05f, 0.85f)
        splitter.splitterProportionKey = "SplitFileEditor.Proportion"
        splitter.firstComponent = flexibleSearchEditor.component

        if (project.isDisposed) {
            splitter.secondComponent = ScrollPaneFactory.createScrollPane(JPanel(), true).apply {
                preferredSize = Dimension(600, 400)
            }
        } else {
            splitter.secondComponent = application.runReadAction<JScrollPane> {
                return@runReadAction buildPropertyForm(project)
            }
        }

        val result = JPanel(BorderLayout())
        result.add(splitter, BorderLayout.CENTER)

        return result
    }

    fun buildPropertyForm(project: Project): JScrollPane {
        if (project.isDisposed) {
            return ScrollPaneFactory.createScrollPane(JPanel(), true).apply {
                preferredSize = Dimension(600, 400)
            }
        }

        val isTsSystemInitialized = isTsSystemInitialized(project)
        var parametersPanel: DialogPanel?

        if (!isTsSystemInitialized) {
            parametersPanel = panel {
                collapsibleGroup("Properties") {
                    row {
                        label("Initializing Type System, Please wait...")
                            .align(Align.CENTER)
                            .resizableColumn()
                    }.resizableRow()
                }
                    .expanded = true
            }
        } else {
            val properties = (PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
                ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java) }
                ?.map { createDefaultFlexibleSearchProperty(it) }
                ?.toMutableSet()
                ?: mutableSetOf())

            putUserData(FLEXIBLE_SEARCH_PROPERTIES_KEY, properties)

            parametersPanel = panel {
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
                }

                group("Notes:") {
                    row {
                        comment("String parameters must be wrapped in single quotes: ''value''.")
                    }
                }

                if (properties.isEmpty()) {
                    row {
                        label("FlexibleSearch query doesn't have parameters")
                            .align(Align.CENTER)
                            .resizableColumn()
                    }
                } else {
                    // Calculate maximum label width, capped at width of 50 characters
                    val dummyLabel = JLabel()
                    val fontMetrics = dummyLabel.getFontMetrics(dummyLabel.font)
                    val maxNameWidth = properties.maxOf { fontMetrics.stringWidth(it.name) }
                    val widthFor50Chars = fontMetrics.stringWidth("W".repeat(50))
                    val labelWidth = minOf(maxNameWidth, widthFor50Chars)

                    group("Parameters:") {
                        properties.forEach { property ->
                            row {
                                val labelText = if (property.name.length > 50) {
                                    property.name.take(47) + "..."
                                } else {
                                    property.name
                                }

                                label(labelText)
                                    .applyToComponent {
                                        preferredSize = Dimension(labelWidth, preferredSize.height)
                                        if (property.name.length > 50) {
                                            toolTipText = property.name
                                        }
                                    }

                                val valueField: JTextField = textField()
                                    .bindText(property::value)
                                    .component

                                valueField.document.addDocumentListener(object : DocumentListener {
                                    override fun insertUpdate(e: DocumentEvent?) = fire()
                                    override fun removeUpdate(e: DocumentEvent?) = fire()
                                    override fun changedUpdate(e: DocumentEvent?) = fire()

                                    private fun fire() {
                                        property.value = valueField.text
                                    }
                                })
                            }
                            if (property.description?.isNotBlank() ?: false) {
                                row {
                                    label(property.description!!)
                                        .applyToComponent {
                                            font = font.deriveFont(font.size2D - 1f)
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ScrollPaneFactory.createScrollPane(parametersPanel, true).apply {
            preferredSize = Dimension(600, 400)
            //isVisible = false
        }
    }

    private fun createDefaultFlexibleSearchProperty(psiElement: FlexibleSearchBindParameter): FlexibleSearchProperty =
        FlexibleSearchProperty(psiElement.text.removePrefix("?"), "", "", "")

    override fun getComponent(): JComponent {
        return flexibleSearchComponent
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return if (flexibleSearchEditor.component.isVisible) flexibleSearchEditor.preferredFocusedComponent else flexibleSearchComponent.getPreferredFocusedComponent()
    }

    override fun getName(): String {
        return "FlexibleSearch Split Editor"
    }

    override fun setState(state: FileEditorState) {
        flexibleSearchEditor.setState(state)
    }

    override fun isModified(): Boolean {
        return flexibleSearchEditor.isModified
    }

    override fun isValid(): Boolean {
        return flexibleSearchEditor.isValid && flexibleSearchComponent.isValid
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        flexibleSearchEditor.addPropertyChangeListener(listener)
        flexibleSearchComponent.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        flexibleSearchEditor.removePropertyChangeListener(listener)
        flexibleSearchComponent.removePropertyChangeListener(listener)
    }

    override fun dispose() {
        Disposer.dispose(flexibleSearchEditor)
    }

    override fun getEditor(): Editor {
        return flexibleSearchEditor.editor
    }

    override fun canNavigateTo(navigatable: Navigatable): Boolean {
        return flexibleSearchEditor.canNavigateTo(navigatable)
    }

    override fun navigateTo(navigatable: Navigatable) {
        flexibleSearchEditor.navigateTo(navigatable)
    }

    override fun getFile(): VirtualFile? {
        return editor.virtualFile
    }

    companion object {
        private const val serialVersionUID: Long = -3770395176190649196L

        val TOPIC_EDITOR_CHANGED = Topic("FXS_EDITOR_CHANGED", FlexibleSearchSplitEditorListener::class.java)
    }

}

data class FlexibleSearchProperty(
    var name: String,
    var operand: String,
    var value: String,
    var description: String? = null,
)
