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
package sap.commerce.toolset.javaee.configurator

import com.intellij.javaee.ExternalResourceManagerEx
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.platform.backend.workspace.WorkspaceModel
import sap.commerce.toolset.cockpitNG.CngConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectPostImportAsyncConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YBackofficeModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

class XsdSchemaConfigurator : ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "XSD Schema"

    override suspend fun postImport(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val project = importContext.project
        val cockpitJarToFile = readAction {
            importContext.chosenHybrisModuleDescriptors
                .find { it is YWebSubModuleDescriptor && it.owner is YBackofficeModuleDescriptor }
                ?.moduleRootPath
                ?.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
                ?.takeIf { it.directoryExists }
                ?.listDirectoryEntries()
                ?.filter { it.extension == "jar" }
                ?.map { it.normalize() }
                ?.mapNotNull { file ->
                    val name = file.name
                    when {
                        name.startsWith("cockpitcore-", true) -> "CORE" to file.pathString
                        name.startsWith("cockpit-data-integration-", true) -> "DATA_INTEGRATION" to file.pathString
                        name.startsWith("cockpitframework-", true) -> "FRAMEWORK" to file.pathString
                        name.startsWith("backoffice-widgets-", true) -> "BO_WIDGETS" to file.pathString
                        else -> null
                    }
                }
                ?.toMap()
        } ?: return

        val namespaces = listOfNotNull(
            cockpitJarToFile["CORE"]?.let {
                listOf(
                    CngConstants.Namespace.CONFIG_HYBRIS to "$it!/schemas/config/hybris/cockpit-configuration-hybris.xsd",
                    "http://www.hybris.com/schema/cockpitng/editor-definition.xsd" to "$it!/schemas/editor-definition.xsd",
                    "http://www.hybris.com/schema/cockpitng/widget-definition.xsd" to "$it!/schemas/widget-definition.xsd",
                    "http://www.hybris.com/schema/cockpitng/action-definition.xsd" to "$it!/schemas/action-definition.xsd",
                    "http://www.hybris.com/schema/cockpitng/widgets.xsd" to "$it!/schemas/widgets.xsd"
                )
            },
            cockpitJarToFile["BO_WIDGETS"]?.let {
                listOf(
                    CngConstants.Namespace.COMPONENT_COMPARE_VIEW to "$it!/schemas/config/cockpitng/compare-view.xsd",
                    CngConstants.Namespace.CONFIG_DEEP_LINK to "$it!/schemas/config/cockpitng/deep-link.xsd",
                    CngConstants.Namespace.CONFIG_EXPLORER_TREE to "$it!/schemas/config/cockpitng/explorer-tree.xsd",
                    CngConstants.Namespace.CONFIG_EXTENDED_SPLIT_LAYOUT to "$it!/schemas/config/cockpitng/extended-split-layout.xsd",
                    CngConstants.Namespace.CONFIG_FULLTEXT_SEARCH to "$it!/schemas/config/cockpitng/fulltext-search.xsd",
                    CngConstants.Namespace.CONFIG_LINKS to "$it!/schemas/config/cockpitng/links.xsd",
                    CngConstants.Namespace.CONFIG_NOTIFICATIONS to "$it!/schemas/config/cockpitng/notification-area.xsd",
                    CngConstants.Namespace.CONFIG_PERSPECTIVE_CHOOSER to "$it!/schemas/config/cockpitng/perspective-chooser.xsd",
                    CngConstants.Namespace.COMPONENT_QUICK_LIST to "$it!/schemas/config/cockpitng/quick-list.xsd",
                    CngConstants.Namespace.CONFIG_REFINE_BY to "$it!/schemas/config/cockpitng/refine-by.xsd",
                    CngConstants.Namespace.CONFIG_SIMPLE_LIST to "$it!/schemas/config/cockpitng/simple-list.xsd",
                    CngConstants.Namespace.COMPONENT_SUMMARY_VIEW to "$it!/schemas/config/cockpitng/summary-view.xsd",
                    CngConstants.Namespace.COMPONENT_VALUE_CHOOSER to "$it!/schemas/config/cockpitng/value-chooser.xsd",
                    CngConstants.Namespace.CONFIG_VIEW_SWITCHER to "$it!/schemas/config/cockpitng/view-switcher.xsd",
                )
            },
            cockpitJarToFile["DATA_INTEGRATION"]?.let {
                listOf(
                    CngConstants.Namespace.CONFIG_ADVANCED_SEARCH to "$it!/schemas/config/cockpitng/advanced-search.xsd",
                    CngConstants.Namespace.CONFIG_COLLECTION_BROWSER to "$it!/schemas/config/cockpitng/collection-browser.xsd",
                    CngConstants.Namespace.CONFIG_DRAG_AND_DROP to "$it!/schemas/config/cockpitng/drag-and-drop.xsd",
                    CngConstants.Namespace.COMPONENT_DYNAMIC_FORMS to "$it!/schemas/config/cockpitng/dynamic-form.xsd",
                    CngConstants.Namespace.CONFIG_GRID_VIEW to "$it!/schemas/config/cockpitng/grid-view.xsd",
                    CngConstants.Namespace.COMPONENT_EDITOR_AREA to "$it!/schemas/config/cockpitng/item-editor.xsd",
                    CngConstants.Namespace.COMPONENT_LIST_VIEW to "$it!/schemas/config/cockpitng/listview.xsd",
                    CngConstants.Namespace.CONFIG_SIMPLE_SEARCH to "$it!/schemas/config/cockpitng/simple-search.xsd",
                    CngConstants.Namespace.COMPONENT_TREE_COLLECTION to "$it!/schemas/config/cockpitng/tree-collection.xsd",
                    CngConstants.Namespace.CONFIG_WIZARD_CONFIG to "$it!/schemas/config/cockpitng/wizard-config.xsd",
                )
            },
            cockpitJarToFile["FRAMEWORK"]?.let {
                listOf(
                    CngConstants.Namespace.CONFIG_DASHBOARD to "$it!/schemas/config/cockpitng/dashboard.xsd",
                )
            },
        )
            .flatten()

        val externalResourceManager = ExternalResourceManagerEx.getInstanceEx()

        backgroundWriteAction {
            namespaces.forEach { (namespace, xsdLocation) ->
                externalResourceManager.addResource(namespace, xsdLocation, project)
            }
        }
    }
}
