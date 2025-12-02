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

package sap.commerce.toolset.cockpitNG

import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.cockpitNG.model.config.Config
import sap.commerce.toolset.cockpitNG.psi.CngPatterns
import sap.commerce.toolset.isHybrisProject
import javax.swing.Icon

class CngConfigDomFileDescription : DomFileDescription<Config>(Config::class.java, CngPatterns.CONFIG_ROOT) {

    override fun getFileIcon(flags: Int): Icon = HybrisIcons.CockpitNG.CONFIG

    override fun isMyFile(file: XmlFile, module: Module?) = super.isMyFile(file, module)
        && hasNamespace(file)
        && file.isHybrisProject

    private fun hasNamespace(file: XmlFile) = file.rootTag
        ?.attributes
        ?.mapNotNull { it.value }
        ?.any { it == CngConstants.Namespace.CONFIG }
        ?: false

    override fun initializeFileDescription() {
        super.initializeFileDescription()
        registerNamespacePolicy(
            CngConstants.COCKPIT_NG_NAMESPACE_KEY,
            CngConstants.Namespace.CONFIG,
            CngConstants.Namespace.CONFIG_HYBRIS,
            CngConstants.Namespace.COMPONENT_EDITOR_AREA,
            CngConstants.Namespace.COMPONENT_DYNAMIC_FORMS,
            CngConstants.Namespace.COMPONENT_SUMMARY_VIEW,
            CngConstants.Namespace.COMPONENT_LIST_VIEW,
            CngConstants.Namespace.COMPONENT_GRID_VIEW,
            CngConstants.Namespace.COMPONENT_COMPARE_VIEW,
            CngConstants.Namespace.COMPONENT_VALUE_CHOOSER,
            CngConstants.Namespace.COMPONENT_QUICK_LIST,
            CngConstants.Namespace.COMPONENT_TREE_COLLECTION,
            CngConstants.Namespace.CONFIG_ADVANCED_SEARCH,
            CngConstants.Namespace.CONFIG_SIMPLE_SEARCH,
            CngConstants.Namespace.CONFIG_WIZARD_CONFIG,
            CngConstants.Namespace.CONFIG_PERSPECTIVE_CHOOSER,
            CngConstants.Namespace.CONFIG_REFINE_BY,
            CngConstants.Namespace.CONFIG_AVAILABLE_LOCALES,
            CngConstants.Namespace.CONFIG_DASHBOARD,
            CngConstants.Namespace.CONFIG_SIMPLE_LIST,
            CngConstants.Namespace.CONFIG_FULLTEXT_SEARCH,
            CngConstants.Namespace.CONFIG_GRID_VIEW,
            CngConstants.Namespace.CONFIG_COMMON,
            CngConstants.Namespace.CONFIG_NOTIFICATIONS,
            CngConstants.Namespace.CONFIG_DRAG_AND_DROP,
            CngConstants.Namespace.CONFIG_EXPLORER_TREE,
            CngConstants.Namespace.CONFIG_EXTENDED_SPLIT_LAYOUT,
            CngConstants.Namespace.CONFIG_COLLECTION_BROWSER,
            CngConstants.Namespace.CONFIG_DEEP_LINK,
            CngConstants.Namespace.CONFIG_VIEW_SWITCHER,
            CngConstants.Namespace.CONFIG_LINKS,
            CngConstants.Namespace.SPRING,
            CngConstants.Namespace.TEST,
        )
    }
}