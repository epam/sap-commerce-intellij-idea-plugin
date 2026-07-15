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

package sap.commerce.toolset.beanSystem.mcp.providers

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.beanSystem.mcp.BSMcpSearchContext
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.meta.BSMetaModelStateService
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier

data class BSMcpSearchResult<out T>(val items: Collection<T>, val total: Int)

/**
 * Strategy behind the `sap_commerce_list_*` bean-system tools. [search] holds the shared pipeline:
 * normalize the name/extension filters, ensure the model is ready, then (inside a read action) fetch,
 * drop nameless classifiers, apply the filters and hand back the matched classifiers plus the total.
 *
 * `name`/`extensionName` are read directly from [BSGlobalMetaClassifier], so the four bean kinds
 * (DTO beans, WS beans, event beans, enums) differ only by the [BSMetaType] passed in the context.
 */
@Service(Service.Level.PROJECT)
class BSMcpDataProvider(private val project: Project) {

    suspend fun <T : BSGlobalMetaClassifier<*>> search(context: BSMcpSearchContext): BSMcpSearchResult<T> {
        ensureBeanSystemReady(project)

        val normalizedFilter = context.filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensions = context.extensions

        val all = readAction { BSMetaModelAccess.getInstance(project).getAll<T>(context.metaType) }
        val matched = all
            .filter { it.name != null }
            .filter { item ->
                (matcher == null || matcher(item.name!!))
                    && (extensions == null || item.extensionName.lowercase() in extensions)
            }
        return BSMcpSearchResult(items = matched, total = all.size)
    }

    /**
     * Turns the common "not ready" states of the bean-system model (indexing / not-yet-built) into
     * actionable tool errors up front, before the model is queried via [BSMetaModelAccess].
     */
    private fun ensureBeanSystemReady(project: Project) {
        if (DumbService.isDumb(project)) error("Project indexing is in progress; retry once indexing completes.")

        val service = BSMetaModelStateService.getInstance(project)
        if (!service.initialized()) {
            service.init()
            error("The bean system model has not been built yet — a build has been triggered. Retry in a few seconds.")
        }
    }

    companion object {
        fun getInstance(project: Project): BSMcpDataProvider = project.service()
    }
}
