/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.exec

import com.intellij.openapi.project.Project
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.project.PropertyService

abstract class ExecConnectionService<T : ExecConnectionSettingsState> {

    abstract var activeConnection: T
    abstract val connections: Set<T>

    abstract fun default(): T
    abstract fun add(settings: T)
    abstract fun remove(settings: T, scope: ExecConnectionScope = settings.scope)
    abstract fun save(settings: Map<ExecConnectionScope, Set<T>>)

    fun save(settings: Collection<T>) = save(
        settings.groupBy { it.scope }
            .mapValues { (_, v) -> v.toSet() }
    )

    fun save(settings: T) {
        remove(settings)
        add(settings)
    }

    fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        .findProperty(key)
        ?: fallback
}