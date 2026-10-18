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
}
