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
import sap.commerce.toolset.HybrisConstants
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
        ?.any { it == CockpitNGConstants.Namespace.CONFIG }
        ?: false

    override fun initializeFileDescription() {
        super.initializeFileDescription()
        registerNamespacePolicy(
            HybrisConstants.COCKPIT_NG_NAMESPACE_KEY,
            CockpitNGConstants.Namespace.CONFIG,
            CockpitNGConstants.Namespace.CONFIG_HYBRIS,
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA,
            CockpitNGConstants.Namespace.COMPONENT_DYNAMIC_FORMS,
            CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW,
            CockpitNGConstants.Namespace.COMPONENT_LIST_VIEW,
            CockpitNGConstants.Namespace.COMPONENT_GRID_VIEW,
            CockpitNGConstants.Namespace.COMPONENT_COMPARE_VIEW,
            CockpitNGConstants.Namespace.COMPONENT_VALUE_CHOOSER,
            CockpitNGConstants.Namespace.COMPONENT_QUICK_LIST,
            CockpitNGConstants.Namespace.COMPONENT_TREE_COLLECTION,
            CockpitNGConstants.Namespace.CONFIG_ADVANCED_SEARCH,
            CockpitNGConstants.Namespace.CONFIG_SIMPLE_SEARCH,
            CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG,
            CockpitNGConstants.Namespace.CONFIG_PERSPECTIVE_CHOOSER,
            CockpitNGConstants.Namespace.CONFIG_REFINE_BY,
            CockpitNGConstants.Namespace.CONFIG_AVAILABLE_LOCALES,
            CockpitNGConstants.Namespace.CONFIG_DASHBOARD,
            CockpitNGConstants.Namespace.CONFIG_SIMPLE_LIST,
            CockpitNGConstants.Namespace.CONFIG_FULLTEXT_SEARCH,
            CockpitNGConstants.Namespace.CONFIG_GRID_VIEW,
            CockpitNGConstants.Namespace.CONFIG_COMMON,
            CockpitNGConstants.Namespace.CONFIG_NOTIFICATIONS,
            CockpitNGConstants.Namespace.CONFIG_DRAG_AND_DROP,
            CockpitNGConstants.Namespace.CONFIG_EXPLORER_TREE,
            CockpitNGConstants.Namespace.CONFIG_EXTENDED_SPLIT_LAYOUT,
            CockpitNGConstants.Namespace.CONFIG_COLLECTION_BROWSER,
            CockpitNGConstants.Namespace.CONFIG_DEEP_LINK,
            CockpitNGConstants.Namespace.CONFIG_VIEW_SWITCHER,
            CockpitNGConstants.Namespace.CONFIG_LINKS,
            CockpitNGConstants.Namespace.SPRING,
            CockpitNGConstants.Namespace.TEST,
        )
    }
}