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
        if (!value.contains(PLACEHOLDER_PREFIX)) return value

        val expanded = StringBuilder(value.length)
        var index = 0

        while (index < value.length) {
            val start = value.indexOf(PLACEHOLDER_PREFIX, index)
            val end = if (start < 0) -1 else value.indexOf(PLACEHOLDER_SUFFIX, start + PLACEHOLDER_PREFIX.length)

            if (start < 0 || end < 0) {
                expanded.append(value, index, value.length)
                break
            }

            val placeholder = value.substring(start, end + PLACEHOLDER_SUFFIX.length)
            val nestedKey = value.substring(start + PLACEHOLDER_PREFIX.length, end)

            expanded.append(value, index, start)
            expanded.append(resolveNested(nestedKey, visited) ?: placeholder)

            index = end + PLACEHOLDER_SUFFIX.length
        }

        return expanded.toString()
    }

    companion object {
        private const val PLACEHOLDER_PREFIX = "\${"
        private const val PLACEHOLDER_SUFFIX = "}"

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
