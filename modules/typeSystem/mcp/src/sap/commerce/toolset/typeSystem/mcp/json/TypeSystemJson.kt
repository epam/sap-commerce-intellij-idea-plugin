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

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import sap.commerce.toolset.typeSystem.meta.model.TSMetaClassifier

/**
 * Emits the trailing block shared by the atomic/collection type renderers: the owning `extension`
 * (when set) and the `custom`/`autoCreate`/`generate` flags (each only when true). The
 * `autoCreate`/`generate` flags are passed in because they are declared per meta-type rather than on
 * the shared [TSMetaClassifier].
 */
internal fun JsonObjectBuilder.putExtensionAndFlags(classifier: TSMetaClassifier<*>, autoCreate: Boolean, generate: Boolean) {
    classifier.extensionName.takeIf { it.isNotBlank() }?.let { put("extension", it) }
    if (classifier.isCustom) put("custom", true)
    if (autoCreate) put("autoCreate", true)
    if (generate) put("generate", true)
}
