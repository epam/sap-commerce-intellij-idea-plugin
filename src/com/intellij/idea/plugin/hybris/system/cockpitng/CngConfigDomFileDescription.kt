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

package com.intellij.idea.plugin.hybris.system.cockpitng

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPatterns
import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import javax.swing.Icon

class CngConfigDomFileDescription : DomFileDescription<Config>(Config::class.java, CngPatterns.CONFIG_ROOT) {

    override fun getFileIcon(flags: Int): Icon = HybrisIcons.CockpitNG.CONFIG

    override fun isMyFile(file: XmlFile, module: Module?) = super.isMyFile(file, module)
        && hasNamespace(file)
        && ProjectSettingsComponent.getInstance(file.project).isHybrisProject()

    private fun hasNamespace(file: XmlFile) = file.rootTag
        ?.attributes
        ?.mapNotNull { it.value }
        ?.any { it == HybrisConstants.SCHEMA_COCKPIT_NG_CONFIG }
        ?: false

    override fun initializeFileDescription() {
        super.initializeFileDescription()
        registerNamespacePolicy(
            HybrisConstants.COCKPIT_NG_NAMESPACE_KEY,
            NAMESPACE_COCKPIT_NG_CONFIG,
            NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS,
            NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA,
            NAMESPACE_COCKPIT_NG_COMPONENT_DYNAMIC_FORMS,
            NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW,
            NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW,
            NAMESPACE_COCKPIT_NG_COMPONENT_GRID_VIEW,
            NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW,
            NAMESPACE_COCKPIT_NG_COMPONENT_VALUE_CHOOSER,
            NAMESPACE_COCKPIT_NG_COMPONENT_QUICK_LIST,
            NAMESPACE_COCKPIT_NG_COMPONENT_TREE_COLLECTION,
            NAMESPACE_COCKPIT_NG_CONFIG_ADVANCED_SEARCH,
            NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_SEARCH,
            NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG,
            NAMESPACE_COCKPIT_NG_CONFIG_PERSPECTIVE_CHOOSER,
            NAMESPACE_COCKPIT_NG_CONFIG_REFINE_BY,
            NAMESPACE_COCKPIT_NG_CONFIG_AVAILABLE_LOCALES,
            NAMESPACE_COCKPIT_NG_CONFIG_DASHBOARD,
            NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_LIST,
            NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH,
            NAMESPACE_COCKPIT_NG_CONFIG_GRID_VIEW,
            NAMESPACE_COCKPIT_NG_CONFIG_COMMON,
            NAMESPACE_COCKPIT_NG_CONFIG_NOTIFICATIONS,
            NAMESPACE_COCKPIT_NG_CONFIG_DRAG_AND_DROP,
            NAMESPACE_COCKPIT_NG_CONFIG_EXPLORER_TREE,
            NAMESPACE_COCKPIT_NG_CONFIG_EXTENDED_SPLIT_LAYOUT,
            NAMESPACE_COCKPIT_NG_CONFIG_COLLECTION_BROWSER,
            NAMESPACE_COCKPIT_NG_CONFIG_DEEP_LINK,
            NAMESPACE_COCKPIT_NG_CONFIG_VIEW_SWITCHER,
            NAMESPACE_COCKPIT_NG_CONFIG_LINKS,
            NAMESPACE_COCKPIT_NG_SPRING,
            NAMESPACE_COCKPIT_NG_TEST,
        )
    }

    companion object {
        const val NAMESPACE_COCKPIT_NG_CONFIG = "http://www.hybris.com/cockpit/config"
        const val NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS = "http://www.hybris.com/cockpit/config/hybris"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA = "http://www.hybris.com/cockpitng/component/editorArea"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_DYNAMIC_FORMS = "http://www.hybris.com/cockpitng/component/dynamicForms"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW = "http://www.hybris.com/cockpitng/component/summaryview"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW = "http://www.hybris.com/cockpitng/component/listView"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_GRID_VIEW = "http://www.hybris.com/cockpitng/component/gridView"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW = "http://www.hybris.com/cockpitng/component/compareview"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_VALUE_CHOOSER = "http://www.hybris.com/cockpitng/component/valuechooser"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_QUICK_LIST = "http://www.hybris.com/cockpitng/component/quick-list"
        const val NAMESPACE_COCKPIT_NG_COMPONENT_TREE_COLLECTION = "http://www.hybris.com/cockpitng/component/treeCollection"
        const val NAMESPACE_COCKPIT_NG_CONFIG_ADVANCED_SEARCH = "http://www.hybris.com/cockpitng/config/advancedsearch"
        const val NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_SEARCH = "http://www.hybris.com/cockpitng/config/simplesearch"
        const val NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG = "http://www.hybris.com/cockpitng/config/wizard-config"
        const val NAMESPACE_COCKPIT_NG_CONFIG_PERSPECTIVE_CHOOSER = "http://www.hybris.com/cockpitng/config/perspectiveChooser"
        const val NAMESPACE_COCKPIT_NG_CONFIG_REFINE_BY = "http://www.hybris.com/cockpitng/config/refineBy"
        const val NAMESPACE_COCKPIT_NG_CONFIG_AVAILABLE_LOCALES = "http://www.hybris.com/cockpitng/config/availableLocales"
        const val NAMESPACE_COCKPIT_NG_CONFIG_DASHBOARD = "http://www.hybris.com/cockpitng/config/dashboard"
        const val NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_LIST = "http://www.hybris.com/cockpitng/config/simplelist"
        const val NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH = "http://www.hybris.com/cockpitng/config/fulltextsearch"
        const val NAMESPACE_COCKPIT_NG_CONFIG_GRID_VIEW = "http://www.hybris.com/cockpitng/config/gridView"
        const val NAMESPACE_COCKPIT_NG_CONFIG_COMMON = "http://www.hybris.com/cockpitng/config/common"
        const val NAMESPACE_COCKPIT_NG_CONFIG_NOTIFICATIONS = "http://www.hybris.com/cockpitng/config/notifications"
        const val NAMESPACE_COCKPIT_NG_CONFIG_DRAG_AND_DROP = "http://www.hybris.com/cockpitng/component/dragAndDrop"
        const val NAMESPACE_COCKPIT_NG_CONFIG_EXPLORER_TREE = "http://www.hybris.com/cockpitng/config/explorertree"
        const val NAMESPACE_COCKPIT_NG_CONFIG_EXTENDED_SPLIT_LAYOUT = "http://www.hybris.com/cockpitng/config/extendedsplitlayout"
        const val NAMESPACE_COCKPIT_NG_CONFIG_COLLECTION_BROWSER = "http://www.hybris.com/cockpitng/config/collectionbrowser"
        const val NAMESPACE_COCKPIT_NG_CONFIG_DEEP_LINK = "http://www.hybris.com/cockpitng/config/deeplink"
        const val NAMESPACE_COCKPIT_NG_CONFIG_VIEW_SWITCHER = "http://www.hybris.com/cockpitng/config/viewSwitcher"
        const val NAMESPACE_COCKPIT_NG_CONFIG_LINKS = "http://www.hybris.com/cockpitng/config/links"
        const val NAMESPACE_COCKPIT_NG_SPRING = "http://www.hybris.com/cockpitng/spring"
        const val NAMESPACE_COCKPIT_NG_TEST = "http://www.hybris.com/cockpit/test"
    }
}