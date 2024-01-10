/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.cockpitng.lang.folding

import com.intellij.idea.plugin.hybris.lang.folding.AbstractXmlFoldingBuilderEx
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.Field
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.FieldList
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.Parameter
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Attribute
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Section
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListColumn
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.ExplorerNode
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.ExplorerTree
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.TypeNode
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.AdditionalParam
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.ComposedHandler
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.Property
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.PropertyList
import com.intellij.idea.plugin.hybris.system.cockpitng.settings.CngFoldingSettings
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag

class CngConfigFoldingBuilder : AbstractXmlFoldingBuilderEx<CngFoldingSettings, Config>(Config::class.java), DumbAware {

    override val filter = PsiElementFilter {
        when (it) {
            is XmlTag -> when (it.localName) {
                FieldList.FIELD,
                Section.ATTRIBUTE,
                ExplorerTree.TYPE_NODE,
                ExplorerTree.NAVIGATION_NODE,
                ListView.COLUMN,
                PropertyList.PROPERTY,
                Property.EDITOR_PARAMETER,
                ComposedHandler.ADDITIONAL_PARAMS,
                Parameter.TAG_NAME -> true

                else -> false
            }

            else -> false
        }
    }

    override fun initSettings(project: Project) = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
        .cngSettings
        .folding

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            ComposedHandler.ADDITIONAL_PARAMS -> fold(psi, ComposedHandler.ADDITIONAL_PARAMS, AdditionalParam.KEY,
                getCachedFoldingSettings(psi)?.tablifyParameters,
                psi.getAttributeValue(AdditionalParam.VALUE)
                    ?.let { " = $it" }
                    ?: ""
            )

            Parameter.TAG_NAME,
            Property.EDITOR_PARAMETER -> {
                val subTags = psi.subTags.associateBy { it.localName }

                // TODO : add tablify

                val name = subTags[Parameter.NAME]
                    ?.value
                    ?.trimmedText
                    ?: "?"
                val value = subTags[Parameter.VALUE]
                    ?.value
                    ?.trimmedText
                    ?.let { " = $it" }
                    ?: " ?"

                name + value
            }

            FieldList.FIELD -> fold(psi, FieldList.FIELD, Field.NAME,
                getCachedFoldingSettings(psi)?.tablifySearchFields,
                computeExtraAttributes(
                    psi.getAttributeValue(Field.MERGE_MODE)?.lowercase(),
                    psi.getAttributeValue(Field.OPERATOR),
                    psi.getAttributeValue(Field.SELECTED)
                        ?.takeIf { it == "true" }
                        ?.let { "pre-selected" }
                ))

            ListView.COLUMN -> fold(psi, ListView.COLUMN, ListColumn.QUALIFIER,
                getCachedFoldingSettings(psi)?.tablifyListColumns,
                computeExtraAttributes(
                    psi.getAttributeValue(ListColumn.SPRING_BEAN),
                    psi.getAttributeValue(ListColumn.SORTABLE)
                        ?.takeIf { it == "true" }
                        ?.let { "sortable" }
                ))

            Section.ATTRIBUTE -> fold(psi, Section.ATTRIBUTE, Attribute.QUALIFIER,
                getCachedFoldingSettings(psi)?.tablifyListColumns,
                computeExtraAttributes(
                    psi.getAttributeValue(Attribute.READONLY)
                        ?.takeIf { "true".equals(it, true) }
                        ?.let { "readonly" },
                    psi.getAttributeValue(Attribute.VISIBLE)
                        ?.takeIf { "false".equals(it, true) }
                        ?.let { "non-visible" },
                    psi.getAttributeValue(Attribute.MERGE_MODE)
                        ?.lowercase()
                )
            )

            ExplorerTree.TYPE_NODE -> fold(psi, ExplorerTree.TYPE_NODE, TypeNode.ID,
                getCachedFoldingSettings(psi)?.tablifyNavigationNodes,
                (psi.getAttributeValue(TypeNode.CODE)
                    ?.let { TYPE_SEPARATOR + it }
                    ?: "") +
                    computeExtraAttributes(
                        psi.getAttributeValue(ExplorerNode.MERGE_MODE),
                    )
            )

            ExplorerTree.NAVIGATION_NODE -> fold(psi, ExplorerTree.NAVIGATION_NODE, ExplorerNode.ID,
                getCachedFoldingSettings(psi)?.tablifyNavigationNodes,
                computeExtraAttributes(
                    psi.getAttributeValue(ExplorerNode.MERGE_MODE),
                )
                    .takeIf { it.isNotBlank() }
                    ?.let { TYPE_SEPARATOR + it }
                    ?: ""
            )

            PropertyList.PROPERTY -> fold(psi, PropertyList.PROPERTY, Property.QUALIFIER,
                getCachedFoldingSettings(psi)?.tablifyWizardProperties,
                (psi.getAttributeValue(Property.TYPE)
                    ?.let { TYPE_SEPARATOR + it }
                    ?: "") +
                    (computeExtraAttributes(
                        psi.getAttributeValue(Property.POSITION),
                        psi.getAttributeValue(Property.MERGE_MODE)?.lowercase(),
                        psi.getAttributeValue(Property.READONLY)
                            ?.takeIf { "true".equals(it, true) }
                            ?.let { "readonly" },
                        psi.getAttributeValue(Property.EXCLUDE)
                            ?.takeIf { "true".equals(it, true) }
                            ?.let { "exclude" },
                    )
                        .takeIf { it.isNotEmpty() }
                        ?.let { " $it" }
                        ?: "")
            )

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    private fun fold(psi: XmlTag, wrapperTagName: String, tagName: String, tablify: Boolean?, extraAttributes: String) = fallbackAttributeValue(psi, tagName)
        .let {
            if (extraAttributes.isNotEmpty()) tablify(psi, it, tablify, wrapperTagName, tagName)
            else it
        } + extraAttributes

    private fun fallbackAttributeValue(psi: XmlTag, fieldName: String, fallback: String = "?"): String = psi.getAttributeValue(fieldName)
        ?.takeIf { it.isNotEmpty() }
        ?: fallback

    private fun computeExtraAttributes(vararg extraAttributes: String?) = extraAttributes
        .filterNotNull()
        .filter { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" | ", "[", "]")
        ?.let { " $it" }
        ?: ""

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            FieldList.FIELD,
            Section.ATTRIBUTE,
            ExplorerTree.TYPE_NODE,
            ListView.COLUMN,
            PropertyList.PROPERTY,
            Property.EDITOR_PARAMETER,
            ComposedHandler.ADDITIONAL_PARAMS,
            Parameter.TAG_NAME -> true

            else -> false
        }

        else -> false
    }

}
