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

package sap.commerce.toolset.typeSystem.mcp.providers

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.typeSystem.mcp.TSMcpSearchContext
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier

/**
 * Strategy behind the `sap_commerce_list_*` type-system tools. Each subclass owns the parts that
 * vary per type — the per-type JSON [itemBuilder] and how the types are [fetched][fetch] — while
 * [search] holds the shared pipeline: normalize the name/extension filters, ensure the model is ready,
 * then (inside a read action) fetch, drop nameless types, apply the filters and render the standard
 * `{<additionalFields>, filter?, extensions?, matched, total, items}` response.
 *
 * `name`/`extensionName` are read directly from [sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier], so only genuinely
 * type-specific behaviour is left to subclasses.
 */
@Service(Service.Level.PROJECT)
class TSMcpDataProvider(private val project: Project) {

    suspend fun <T : TSGlobalMetaClassifier<*>> search(context: TSMcpSearchContext): Collection<T> {
        val normalizedFilter = context.filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensions = context.extensions

        ensureTypeSystemReady(project)

        return readAction {
            TSMetaModelAccess.getInstance(project).getAll<T>(context.metaType)
                .filter { it.name != null }
                .filter { item ->
                    (matcher == null || matcher(item.name!!))
                        && (extensions == null || item.extensionName.lowercase() in extensions)
                }
//            buildListResponse(
//                items = matched,
//                total = candidates.size,
//                itemBuilder = itemBuilder,
//                filterText = normalizedFilter,
//                additionalFields = {
//                    additionalFields()
//                    extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
//                },
//            )
        }
    }

    /**
     * Turns the common "not ready" states of the type-system model (indexing / not-yet-built) into
     * actionable tool errors up front, before the model is queried via [TSMetaModelAccess].
     *
     * Retrieval still resolves through [sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService.Companion.state], which may throw
     * [com.intellij.openapi.progress.ProcessCanceledException] if a rebuild is in flight; that must
     * NOT be swallowed, so it is intentionally left to propagate.
     */
    private fun ensureTypeSystemReady(project: Project) {
        if (DumbService.isDumb(project)) error("Project indexing is in progress; retry once indexing completes.")

        val service = TSMetaModelStateService.getInstance(project)
        if (!service.initialized()) {
            service.init()
            error("The type system model has not been built yet — a build has been triggered. Retry in a few seconds.")
        }
    }

    companion object {
        fun getInstance(project: Project): TSMcpDataProvider = project.service()
    }
}