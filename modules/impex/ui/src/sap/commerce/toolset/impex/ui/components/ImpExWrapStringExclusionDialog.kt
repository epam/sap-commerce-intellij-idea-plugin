/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.ui.components

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.addDocumentListener
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.ui.ImpExWrapStringExclusion
import sap.commerce.toolset.typeSystem.codeInsight.lookup.TSLookupElementFactory
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import sap.commerce.toolset.ui.banner
import java.awt.Component
import java.awt.Dimension

class ImpExWrapStringExclusionDialog(
    private val project: Project,
    private val exclusion: ImpExWrapStringExclusion,
    parentComponent: Component,
    dialogTitle: String,
) : DialogWrapper(project, parentComponent, false, IdeModalityType.PROJECT) {

    private val selectedMetaItem = AtomicProperty(
        exclusion.typeName.let { TSMetaModelAccess.getInstance(project).findMetaItemByName(it) }
    )

    init {
        title = dialogTitle
        isResizable = false

        super.init()
    }

    override fun getInitialSize() = Dimension(450, super.initialSize?.height ?: 125)

    override fun createNorthPanel() = banner(
        text = """
            Only existing type and attribute are allowed.<br>
            Use code completion to get list of available types and their attributes.
            """.trimIndent(),
        status = EditorNotificationPanel.Status.Info
    )

    override fun createCenterPanel(): DialogPanel {
        val itemTextFieldCompletion = TextFieldWithAutoCompletion.create(
            project, emptyList(), false, exclusion.typeName
        )
            .apply { installProvider(ItemListProvider(project)) }
            .apply {
                addDocumentListener(disposable, object : DocumentListener {
                    override fun documentChanged(event: DocumentEvent) {
                        val name = event.document.text.trim()
                        val metaItem = TSMetaModelAccess.getInstance(project).findMetaItemByName(name)

                        selectedMetaItem.set(metaItem)
                    }
                })
            }
        val attributeTextFieldCompletion = TextFieldWithAutoCompletion.create(
            project, emptyList(), false, exclusion.attributeName
        ).apply { installProvider(AttributeListProvider(selectedMetaItem)) }

        return panel {
            row {
                icon(HybrisIcons.TypeSystem.Types.ITEM)
                    .gap(RightGap.SMALL)
                cell(itemTextFieldCompletion)
                    .align(AlignX.FILL)
                    .validationOnApply {
                        val metaItem = TSMetaModelAccess.getInstance(project).findMetaItemByName(it.text)
                        if (metaItem == null) error("Please enter a valid item")
                        else null
                    }
                    .label("Item type:")
            }.layout(RowLayout.PARENT_GRID)

            row {
                icon(HybrisIcons.TypeSystem.ATTRIBUTE)
                    .gap(RightGap.SMALL)
                cell(attributeTextFieldCompletion)
                    .label("Attribute:")
                    .align(AlignX.FILL)
                    .validationOnApply {
                        val metaItem = selectedMetaItem.get() ?: return@validationOnApply null

                        if (metaItem.attributes.contains(it.text)) null
                        else error("Please enter a logger name")
                    }
                    .enabledIf(selectedMetaItem.isNotNull())
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    private class ItemListProvider(private val project: Project) : TextFieldWithAutoCompletionListProvider<TSGlobalMetaItem>(emptyList()) {
        override fun getItems(prefix: String?, cached: Boolean, parameters: CompletionParameters?): Collection<TSGlobalMetaItem?> {
            if (prefix == null) return emptyList()

            return TSMetaModelAccess.getInstance(project).getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
                .filter { it.name?.startsWith(prefix, true) ?: false }
        }

        override fun createLookupBuilder(item: TSGlobalMetaItem) = TSLookupElementFactory.build(item)
            ?: super.createLookupBuilder(item)

        override fun getLookupString(item: TSGlobalMetaItem) = item.name
            ?: item.toString()

        override fun getIcon(item: TSGlobalMetaItem) = item.icon
    }

    private class AttributeListProvider(
        private val selectedMetaItem: AtomicProperty<TSGlobalMetaItem?>,
    ) : TextFieldWithAutoCompletionListProvider<TSGlobalMetaItem.TSGlobalMetaItemAttribute>(emptyList()) {

        override fun getItems(prefix: String?, cached: Boolean, parameters: CompletionParameters?): Collection<TSGlobalMetaItem.TSGlobalMetaItemAttribute?> {
            if (prefix == null) return emptyList()

            val metaItem = selectedMetaItem.get() ?: return emptyList()

            return metaItem.allAttributes.values
                .filter { it.name.startsWith(prefix, true) ?: false }
        }

        override fun createLookupBuilder(item: TSGlobalMetaItem.TSGlobalMetaItemAttribute) = TSLookupElementFactory.build(item)
        override fun getLookupString(item: TSGlobalMetaItem.TSGlobalMetaItemAttribute) = item.name
        override fun getIcon(item: TSGlobalMetaItem.TSGlobalMetaItemAttribute) = item.icon
    }
}