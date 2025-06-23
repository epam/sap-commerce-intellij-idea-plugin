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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchDefinedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchYColumnName
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.getPreferredFocusedComponent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.dsl.builder.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FlexibleSearchSplitEditor : UserDataHolderBase, FileEditor, TextEditor {

    private val flexibleSearchEditor: TextEditor
    private val flexibleSearchComponent: JComponent

    constructor(e: TextEditor, project: Project) : super() {
        flexibleSearchEditor = e
        flexibleSearchComponent = createComponent(project)
    }

    private fun createComponent(project: Project): JComponent {
        val splitter = JBSplitter(false, 0.07f, 0.05f, 0.85f)
        splitter.splitterProportionKey = "SplitFileEditor.Proportion"
        splitter.firstComponent = flexibleSearchEditor.component
        splitter.secondComponent = buildPropertyForm(project)

        val result = JPanel(BorderLayout())
        result.add(splitter, BorderLayout.CENTER)

        return result
    }

    fun findColumnName(psiElement: PsiElement): FlexibleSearchYColumnName? {
        return PsiTreeUtil.findChildOfType(psiElement, FlexibleSearchYColumnName::class.java)
            ?: findColumnNameInternal(psiElement, 0)
    }

    fun findColumnNameInternal(psiElement: PsiElement, depth: Int): FlexibleSearchYColumnName? {
        return if (depth > 100) return null
        else PsiTreeUtil.findChildOfType(psiElement, FlexibleSearchYColumnName::class.java)
            ?: findColumnNameInternal(psiElement.parent, depth + 1)
    }

    fun findTypeName(psiElement: PsiElement): FlexibleSearchDefinedTableName? {
        return PsiTreeUtil.findChildOfType(psiElement, FlexibleSearchDefinedTableName::class.java)
            ?: findTypeNameInternal(psiElement, 0)
    }

    fun findTypeNameInternal(psiElement: PsiElement, depth: Int): FlexibleSearchDefinedTableName? {
        return if (depth > 100) return null
        else PsiTreeUtil.findChildOfType(psiElement, FlexibleSearchDefinedTableName::class.java)
            ?: findTypeNameInternal(psiElement.parent, depth + 1)
    }

    fun createFlexibleSearchProperty(psiElement: FlexibleSearchBindParameter, project: Project): FlexibleSearchProperty {
        val typeName = findTypeName(psiElement.parent) ?: return createDefaultFlexibleSearchProperty(psiElement)
        val columnName = findColumnName(psiElement.parent) ?: return createDefaultFlexibleSearchProperty(psiElement)
        TSMetaModelAccess.getInstance(project).findMetaItemByName(typeName.text)
            ?.attributes
            ?.get(columnName.text)
            ?.let { attr -> attr.type?.let { type -> return FlexibleSearchProperty(attr, psiElement.text.removePrefix("?"), "", "", attr.description)} }


        println("Found column name: ${typeName.text}.${columnName.text}")
        return createDefaultFlexibleSearchProperty(psiElement)
    }

    private fun createDefaultFlexibleSearchProperty(psiElement: FlexibleSearchBindParameter): FlexibleSearchProperty = FlexibleSearchProperty(null, psiElement.text.removePrefix("?"), "", "", "")

    fun buildPropertyForm(project: Project): JScrollPane {
        val properties = (PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            ?.let {
                PsiTreeUtil.findChildrenOfType(it, FlexibleSearchBindParameter::class.java)
            }
            ?.map { it -> createFlexibleSearchProperty(it, project) }
            ?.toMutableSet()
            ?: mutableSetOf())

        putUserData(FLEXIBLE_SEARCH_PROPERTIES_KEY, properties)


        val panel = panel {
            if (properties.isEmpty()) {
                row {
                    label("FlexibleSearch query doesn't have parameters")
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
                                    // Call the callback with the updated list
                                    property.value = valueField.text
                                    println("Property ${property.name} changed to ${valueField.text}")
                                }

                            })

                        }
                        if (!property.description.isNullOrBlank()) {
                            row {
                                label(property.description ?: "")
                                    .applyToComponent {
                                        font = font.deriveFont(font.size2D - 1f)
                                    }
                            }
                        }
                    }
                }
            }
        }

        return ScrollPaneFactory.createScrollPane(panel, true).apply {
            preferredSize = Dimension(600, 400)
        }
    }

    override fun getComponent(): JComponent {
        return flexibleSearchComponent
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return if (flexibleSearchEditor.component.isVisible) flexibleSearchEditor.preferredFocusedComponent else flexibleSearchComponent.getPreferredFocusedComponent()
    }

    override fun getName(): String {
        return "FlexibleSearch split editor"
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
        private const val serialVersionUID: Long = -3185914436741110761L
    }
}

data class FlexibleSearchProperty(
    var attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute?,
    var name: String,
    var operand: String,
    var value: String,
    var description: String? = null,
    var type: FieldType = FieldType.TEXT
)

enum class FieldType {
    TEXT, DATE, NUMBER, BOOLEAN
}