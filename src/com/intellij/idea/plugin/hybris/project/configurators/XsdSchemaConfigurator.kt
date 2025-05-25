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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription
import com.intellij.javaee.ExternalResourceManagerEx
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import java.nio.file.Path
import kotlin.io.path.exists

class XsdSchemaConfigurator {

    fun configureAfterImport(
        project: Project,
        moduleDescriptors: List<ModuleDescriptor>,
    ): List<() -> Unit> {
        val cockpitJarToFile = moduleDescriptors
            .firstOrNull { it.name == HybrisConstants.EXTENSION_NAME_BACK_OFFICE }
            ?.moduleRootDirectory
            ?.toPath()
            ?.resolve(Path.of("web", "webroot", "WEB-INF", "lib"))
            ?.takeIf { it.exists() }
            ?.toFile()
            ?.listFiles { _, name -> name.endsWith(".jar", true) }
            ?.mapNotNull { file ->
                val name = file.name
                when {
                    name.startsWith("cockpitcore-", true) -> "CORE" to file.absolutePath
                    name.startsWith("cockpit-data-integration-", true) -> "DATA_INTEGRATION" to file.absolutePath
                    name.startsWith("cockpitframework-", true) -> "FRAMEWORK" to file.absolutePath
                    name.startsWith("backoffice-widgets-", true) -> "BO_WIDGETS" to file.absolutePath
                    else -> null
                }
            }
            ?.toMap()
            ?: return emptyList()

        return listOf() {
            runWriteAction {
                val externalResourceManager = ExternalResourceManagerEx.getInstanceEx()

                listOfNotNull(
                    cockpitJarToFile["CORE"]?.let {
                        listOf(
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS to "$it!/schemas/config/hybris/cockpit-configuration-hybris.xsd",
                            "http://www.hybris.com/schema/cockpitng/editor-definition.xsd" to "$it!/schemas/editor-definition.xsd",
                            "http://www.hybris.com/schema/cockpitng/widget-definition.xsd" to "$it!/schemas/widget-definition.xsd",
                            "http://www.hybris.com/schema/cockpitng/action-definition.xsd" to "$it!/schemas/action-definition.xsd",
                            "http://www.hybris.com/schema/cockpitng/widgets.xsd" to "$it!/schemas/widgets.xsd"
                        )
                    },
                    cockpitJarToFile["BO_WIDGETS"]?.let {
                        listOf(
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW to "$it!/schemas/config/cockpitng/compare-view.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_DEEP_LINK to "$it!/schemas/config/cockpitng/deep-link.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_EXPLORER_TREE to "$it!/schemas/config/cockpitng/explorer-tree.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_EXTENDED_SPLIT_LAYOUT to "$it!/schemas/config/cockpitng/extended-split-layout.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH to "$it!/schemas/config/cockpitng/fulltext-search.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_LINKS to "$it!/schemas/config/cockpitng/links.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_NOTIFICATIONS to "$it!/schemas/config/cockpitng/notification-area.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_PERSPECTIVE_CHOOSER to "$it!/schemas/config/cockpitng/perspective-chooser.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_QUICK_LIST to "$it!/schemas/config/cockpitng/quick-list.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_REFINE_BY to "$it!/schemas/config/cockpitng/refine-by.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_LIST to "$it!/schemas/config/cockpitng/simple-list.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW to "$it!/schemas/config/cockpitng/summary-view.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_VALUE_CHOOSER to "$it!/schemas/config/cockpitng/value-chooser.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_VIEW_SWITCHER to "$it!/schemas/config/cockpitng/view-switcher.xsd",
                        )
                    },
                    cockpitJarToFile["DATA_INTEGRATION"]?.let {
                        listOf(
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_ADVANCED_SEARCH to "$it!/schemas/config/cockpitng/advanced-search.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_COLLECTION_BROWSER to "$it!/schemas/config/cockpitng/collection-browser.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_DRAG_AND_DROP to "$it!/schemas/config/cockpitng/drag-and-drop.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_DYNAMIC_FORMS to "$it!/schemas/config/cockpitng/dynamic-form.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_GRID_VIEW to "$it!/schemas/config/cockpitng/grid-view.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA to "$it!/schemas/config/cockpitng/item-editor.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW to "$it!/schemas/config/cockpitng/listview.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_SEARCH to "$it!/schemas/config/cockpitng/simple-search.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_TREE_COLLECTION to "$it!/schemas/config/cockpitng/tree-collection.xsd",
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG to "$it!/schemas/config/cockpitng/wizard-config.xsd",
                        )
                    },
                    cockpitJarToFile["FRAMEWORK"]?.let {
                        listOf(
                            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_DASHBOARD to "$it!/schemas/config/cockpitng/dashboard.xsd",
                        )
                    },
                )
                    .flatten()
                    .forEach { (namespace, xsdLocation) ->
                        externalResourceManager.addResource(namespace, xsdLocation, project)
                    }
            }
        }
    }

}
