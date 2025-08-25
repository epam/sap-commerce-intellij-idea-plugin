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

package sap.commerce.toolset.logging.ui.tree.nodes

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.CxLoggersConstants
import sap.commerce.toolset.logging.LogLevel

class BundledLoggersTemplateGroupNode(project: Project) : LoggersOptionsNode("Bundled Loggers Templates", HybrisIcons.Log.Template.TEMPLATES, project) {

    override fun getNewChildren(nodeParameters: LoggersNodeParameters): Map<String, LoggersNode> {
        val content = ExtensionsService.getInstance().findResource(CxLoggersConstants.CX_LOGGERS_BUNDLED)

        return listOf(
            BundledLoggersTemplateItemNode(
                "Enabled SOLR loggers",
                listOf(
                    CxLoggerModel.of("de.hybris.platform.solrfacetsearch.search.impl.DefaultFacetSearchStrategy", LogLevel.DEBUG.name)
                ),
                project
            ),
            BundledLoggersTemplateItemNode(
                "Disabled SOLR loggers",
                listOf(
                    CxLoggerModel.of("de.hybris.platform.solrfacetsearch.search.impl.DefaultFacetSearchStrategy", LogLevel.OFF.name)
                ),
                project
            ),
            BundledLoggersTemplateItemNode(
                "Enabled FlexibleSearch loggers",
                listOf(
                    CxLoggerModel.of("de.hybris.platform.jalo.flexiblesearch", LogLevel.DEBUG.name)
                ),
                project
            ),
            BundledLoggersTemplateItemNode(
                "Disabled FlexibleSearch loggers",
                listOf(
                    CxLoggerModel.of("de.hybris.platform.jalo.flexiblesearch", LogLevel.OFF.name)
                ),
                project
            )
        )
            .associateBy { it.name }
    }
}

class BundledLoggersTemplateItemNode(
    private val text: String,
    val loggers: List<CxLoggerModel>,
    project: Project
) : LoggersOptionsNode(text, HybrisIcons.Log.Template.BUNDLED, project)