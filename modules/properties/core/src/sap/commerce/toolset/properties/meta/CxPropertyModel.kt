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

package sap.commerce.toolset.properties.meta

/**
 * Immutable snapshot of the local SAP Commerce property chain.
 *
 * Keeps every declaration of every property, so that both the winning value and the shadowed ones stay available,
 * and applies the platform resolution rules on top of it:
 * 1. the declaration bound to the greatest [CxPropertySource] wins, see [CxPropertyScope] for the tier order;
 * 2. `${...}` placeholders of the winning value are expanded against the very same chain.
 *
 * Placeholders which cannot be expanded — unknown properties and reference cycles — are left untouched.
 */
class CxPropertyModel private constructor(
    val properties: Map<String, CxProperty>,
    val sources: List<CxPropertySource>,
) {

    val keys
        get() = properties.keys

    operator fun get(key: String) = properties[key]

    operator fun contains(key: String) = properties.containsKey(key)

    /** Value the platform would apply for the [key], with all placeholders expanded. */
    fun resolve(key: String): String? = properties[key]
        ?.rawValue
        ?.let { expand(it, setOf(key)) }

    /** Every resolved property of the chain, ordered by key. */
    fun resolveAll(): Map<String, String> = properties.keys
        .mapNotNull { key -> resolve(key)?.let { key to it } }
        .toMap(LinkedHashMap())

    /**
     * Resolves the given [keys] into the properties the project actually declares, in their iteration order, each
     * carrying the file it was declared in. Keys the project does not declare are left out, so a caller can tell
     * "declared as something else" apart from "not declared here at all".
     */
    fun resolveProperties(keys: Collection<String>): Map<String, CxResolvedProperty> = keys
        .distinct()
        .mapNotNull { key ->
            val property = properties[key] ?: return@mapNotNull null
            val value = resolve(key) ?: return@mapNotNull null

            CxResolvedProperty.of(property, value)?.let { key to it }
        }
        .toMap(LinkedHashMap())

    /** Declarations contributed by the [source], ordered by key. */
    fun declarationsIn(source: CxPropertySource) = properties.values
        .mapNotNull { property -> property.declarations.find { it.source == source } }

    private fun resolveNested(key: String, visited: Set<String>): String? {
        if (key in visited) return null

        return properties[key]
            ?.rawValue
            ?.let { expand(it, visited + key) }
    }

    private fun expand(value: String, visited: Set<String>): String {
        val placeholders = CxPropertyPlaceholder.of(value)
        if (placeholders.isEmpty()) return value

        val expanded = StringBuilder(value.length)
        var index = 0

        placeholders.forEach { placeholder ->
            expanded.append(value, index, placeholder.range.first)
            // A placeholder which cannot be expanded is left exactly as it is written, which is what the platform does.
            expanded.append(resolveNested(placeholder.key, visited) ?: value.substring(placeholder.range.first, placeholder.range.last + 1))
            index = placeholder.range.last + 1
        }

        return expanded.append(value, index, value.length).toString()
    }

    companion object {
        val EMPTY = CxPropertyModel(emptyMap(), emptyList())

        fun of(sources: Collection<CxPropertySource>, declarations: Collection<CxPropertyDeclaration>) = CxPropertyModel(
            declarations
                .groupBy { it.key }
                .mapValues { (key, keyDeclarations) -> CxProperty.of(key, keyDeclarations) }
                .toSortedMap(),
            sources.distinct().sorted(),
        )
    }
}
