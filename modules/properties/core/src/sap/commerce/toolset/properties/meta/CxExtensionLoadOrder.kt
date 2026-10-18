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
 * Reproduces the order in which the platform loads `project.properties` of the configured extensions: an extension is
 * always loaded after everything it requires, so that its own values override the inherited ones.
 *
 * Requirement cycles and requirements pointing to extensions which are not part of the project are tolerated — the
 * platform resolves them the very same way, by ignoring the offending edge.
 */
object CxExtensionLoadOrder {

    /**
     * @param requirements extension name to the names of the extensions it requires, iterated in the declaration order.
     * @return every extension of [requirements], ordered from the first to the last loaded one.
     */
    fun of(requirements: Map<String, Collection<String>>): List<String> {
        val ordered = LinkedHashSet<String>(requirements.size)
        val visiting = HashSet<String>()

        requirements.keys.forEach { visit(it, requirements, ordered, visiting) }

        return ordered.toList()
    }

    /** @return extension name to its position in the load order. */
    fun ranks(requirements: Map<String, Collection<String>>) = of(requirements)
        .withIndex()
        .associate { (rank, name) -> name to rank }

    private fun visit(
        name: String,
        requirements: Map<String, Collection<String>>,
        ordered: LinkedHashSet<String>,
        visiting: MutableSet<String>,
    ) {
        if (name in ordered) return

        val required = requirements[name] ?: return
        if (!visiting.add(name)) return

        required.forEach { visit(it, requirements, ordered, visiting) }

        visiting.remove(name)
        ordered.add(name)
    }
}
