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

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * Compares a remote instance against the project's own property chain.
 *
 * "Declared by the project" means declared by a source the running system actually reads: a declaration living in an
 * extension which `localextensions.xml` does not list counts as missing, because the platform would never load it.
 * [CxProperty.isIgnored] tells the two apart when a report needs to explain itself.
 */
object CxPropertyComparison {

    /** Remote properties which the project does not declare, ordered by key. */
    fun missingInProject(remote: Collection<CxPropertyPresentation>, chain: CxPropertyModel) = remote
        .filter { chain.resolve(it.key) == null }
        .sortedBy { it.key }

    /** Project properties which the remote instance does not have, carrying the value the project resolves to. */
    fun missingOnRemote(remote: Collection<CxPropertyPresentation>, chain: CxPropertyModel): List<CxPropertyPresentation> {
        val remoteKeys = remote.mapTo(HashSet(remote.size)) { it.key }

        return chain.resolveAll()
            .filterKeys { it !in remoteKeys }
            .map { (key, value) -> CxPropertyPresentation(key, value) }
            .sortedBy { it.key }
    }

    /** Properties both sides declare with different values, ordered by key; [declared] is what the project resolves to. */
    fun differing(remote: Collection<CxPropertyPresentation>, declared: Map<String, String>) = remote
        .mapNotNull { remoteProperty ->
            declared[remoteProperty.key]
                ?.takeIf { it != remoteProperty.value }
                ?.let { CxPropertyDifference(remoteProperty.key, it, remoteProperty.value) }
        }
        .sortedBy { it.key }

    /** Properties of [first] which [second] does not have at all, ordered by key. */
    fun onlyIn(
        first: Collection<CxPropertyPresentation>,
        second: Collection<CxPropertyPresentation>,
    ): List<CxPropertyPresentation> {
        val known = second.mapTo(HashSet(second.size)) { it.key }

        return first
            .filterNot { it.key in known }
            .sortedBy { it.key }
    }

    /**
     * Properties both sides have and disagree about, carrying [first]'s value.
     *
     * The other value is deliberately left to the caller: whichever side is being listed, the counterpart is what
     * explains the difference, and it is already modelled as one.
     */
    fun differing(
        first: Collection<CxPropertyPresentation>,
        second: Collection<CxPropertyPresentation>,
    ): List<CxPropertyPresentation> {
        val other = second.associate { it.key to it.value }

        return first
            .filter { other[it.key]?.let { value -> value != it.value } == true }
            .sortedBy { it.key }
    }
}
