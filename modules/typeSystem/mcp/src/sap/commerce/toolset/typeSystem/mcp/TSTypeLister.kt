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

package sap.commerce.toolset.typeSystem.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.ai.mcp.json.buildListResponse
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.typeSystem.mcp.json.AtomicTypeJsonBuilder
import sap.commerce.toolset.typeSystem.mcp.json.CollectionTypeJsonBuilder
import sap.commerce.toolset.typeSystem.mcp.json.ItemTypeJsonBuilder
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.*

/**
 * Strategy behind the `sap_commerce_list_*` type-system tools. Each subclass owns the parts that
 * vary per type — the per-type JSON [itemBuilder] and how the types are [fetched][fetch] — while
 * [list] holds the shared pipeline: normalize the name/extension filters, ensure the model is ready,
 * then (inside a read action) fetch, drop nameless types, apply the filters and render the standard
 * `{<additionalFields>, filter?, extensions?, matched, total, items}` response.
 *
 * `name`/`extensionName` are read directly from [TSGlobalMetaClassifier], so only genuinely
 * type-specific behaviour is left to subclasses.
 */
sealed class TSTypeLister<T : TSGlobalMetaClassifier<*>>(
    private val itemBuilder: McpJsonBuilder<T>,
) {

    /** Retrieves all types of this kind from the local type-system model. */
    protected abstract fun fetch(meta: TSMetaModelAccess): Collection<T>

    /** Contributes tool-specific leading fields (e.g. the item-type `detail` level). */
    protected open fun JsonObjectBuilder.additionalFields() {}

    suspend fun list(filter: String?, extensions: String?): String {
        val project = currentCoroutineContext().project

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensionFilter = parseExtensionFilter(extensions)

        ensureTypeSystemReady(project)

        return readAction {
            val candidates = fetch(TSMetaModelAccess.getInstance(project))
                .filter { it.name != null }
            val matched = candidates.filter { item ->
                (matcher == null || matcher(item.name!!)) &&
                    (extensionFilter == null || item.extensionName.lowercase() in extensionFilter)
            }
            buildListResponse(
                items = matched,
                total = candidates.size,
                itemBuilder = itemBuilder,
                filterText = normalizedFilter,
                additionalFields = {
                    additionalFields()
                    extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
                },
            )
        }
    }

    private fun parseExtensionFilter(extensions: String?): Set<String>? = extensions
        ?.split(',')
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?.takeIf { it.isNotEmpty() }

    /**
     * Turns the common "not ready" states of the type-system model (indexing / not-yet-built) into
     * actionable tool errors up front, before the model is queried via [TSMetaModelAccess].
     *
     * Retrieval still resolves through [TSMetaModelStateService.state], which may throw
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
}

/** Lists Item types; carries the requested [detail] level, echoed as `detail` and driving the builder. */
class ItemTypeLister(private val detail: ItemTypeDetail) : TSTypeLister<TSGlobalMetaItem>(ItemTypeJsonBuilder(detail)) {
    override fun fetch(meta: TSMetaModelAccess): Collection<TSGlobalMetaItem> = meta.getAll(TSMetaType.META_ITEM)
    override fun JsonObjectBuilder.additionalFields() {
        put("detail", detail.name)
    }
}

/** Lists Atomic types. */
object AtomicTypeLister : TSTypeLister<TSGlobalMetaAtomic>(AtomicTypeJsonBuilder) {
    override fun fetch(meta: TSMetaModelAccess): Collection<TSGlobalMetaAtomic> = meta.getAll(TSMetaType.META_ATOMIC)
}

/** Lists Collection types. */
object CollectionTypeLister : TSTypeLister<TSGlobalMetaCollection>(CollectionTypeJsonBuilder) {
    override fun fetch(meta: TSMetaModelAccess): Collection<TSGlobalMetaCollection> = meta.getAll(TSMetaType.META_COLLECTION)
}
