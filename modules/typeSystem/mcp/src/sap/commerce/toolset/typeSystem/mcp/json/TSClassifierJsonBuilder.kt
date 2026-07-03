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

package sap.commerce.toolset.typeSystem.mcp.json

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import sap.commerce.toolset.ai.mcp.json.McpJsonResponseElementBuilder
import sap.commerce.toolset.ai.mcp.json.putFlag
import sap.commerce.toolset.ai.mcp.json.putIfNotBlank
import sap.commerce.toolset.typeSystem.meta.model.TSMetaClassifier

/**
 * Base [McpJsonResponseElementBuilder] for the simple, single-object type renderers (atomic and collection types).
 *
 * It renders the shape shared by those types: the subclass-specific leading fields (via
 * [putIdentity]) followed by the common trailing block — the owning `extension` (when set) and the
 * `custom`/`autoCreate`/`generate` flags (each only when true). `autoCreate`/`generate` are read
 * through [isAutoCreate]/[isGenerate] because, unlike `extension`/`custom`, they are declared per
 * meta-type rather than on the shared [TSMetaClassifier].
 */
abstract class TSClassifierJsonBuilder<T : TSMetaClassifier<*>> : McpJsonResponseElementBuilder<T> {

    final override fun build(item: T): JsonObject = buildJsonObject {
        putIdentity(item)
        putIfNotBlank("extension", item.extensionName)
        putFlag("custom", item.isCustom)
        putFlag("autoCreate", isAutoCreate(item))
        putFlag("generate", isGenerate(item))
    }

    /** Emits the type-specific leading fields (`name` and, e.g., `extends`/`kind`/`elementType`). */
    protected abstract fun JsonObjectBuilder.putIdentity(item: T)

    protected abstract fun isAutoCreate(item: T): Boolean

    protected abstract fun isGenerate(item: T): Boolean
}
