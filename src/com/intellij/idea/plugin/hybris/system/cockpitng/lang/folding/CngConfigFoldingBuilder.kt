/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Attribute
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Section
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListColumn
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.ExplorerTree
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.TypeNode
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
                ListView.COLUMN,
                PropertyList.PROPERTY -> true

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
            FieldList.FIELD -> psi.getAttributeValue(Field.NAME)
            ListView.COLUMN -> psi.getAttributeValue(ListColumn.QUALIFIER)

            Section.ATTRIBUTE -> psi.getAttributeValue(Attribute.QUALIFIER) + (
                psi.getAttributeValue(Attribute.READONLY)
                    ?.takeIf { "true".equals(it, true) }
                    ?.let { TYPE_SEPARATOR + "readonly" }
                    ?: ""
                ) + (
                psi.getAttributeValue(Attribute.VISIBLE)
                    ?.takeIf { "false".equals(it, true) }
                    ?.let { TYPE_SEPARATOR + "non-visible" }
                    ?: ""
                )

            ExplorerTree.TYPE_NODE -> psi.getAttributeValue(TypeNode.ID)
                ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyNavigationNodes, ExplorerTree.TYPE_NODE, TypeNode.ID) } + (
                psi.getAttributeValue(TypeNode.CODE)
                    ?.let { TYPE_SEPARATOR + it }
                    ?: ""
                )

            PropertyList.PROPERTY -> psi.getAttributeValue(Property.QUALIFIER)
                ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyWizardProperties, PropertyList.PROPERTY, Property.QUALIFIER) } +
                (psi.getAttributeValue(Property.TYPE)
                    ?.let { TYPE_SEPARATOR + it }
                    ?: "")

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            FieldList.FIELD,
            Section.ATTRIBUTE,
            ExplorerTree.TYPE_NODE,
            ListView.COLUMN,
            PropertyList.PROPERTY -> true

            else -> false
        }

        else -> false
    }

}
