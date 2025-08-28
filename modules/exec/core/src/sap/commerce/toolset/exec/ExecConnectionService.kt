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
import sap.commerce.toolset.exec.settings.event.ExecConnectionListener
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.project.PropertyService

abstract class ExecConnectionService<T : ExecConnectionSettingsState>(protected val project: Project) {

    abstract var activeConnection: T
    abstract val connections: List<T>

    protected abstract val listener: ExecConnectionListener<T>
    protected abstract fun save(settings: Map<ExecConnectionScope, List<T>>, notify: Boolean = true)

    protected fun onActivate(settings: T, notify: Boolean = true) = if (notify) listener.onActive(settings) else Unit
    protected fun onAdd(settings: T, notify: Boolean = true) = if (notify) listener.onAdded(settings) else Unit
    protected fun onRemove(settings: T, notify: Boolean = true) = if (notify) listener.onRemoved(settings) else Unit
    protected fun onSave(settings: Map<ExecConnectionScope, List<T>>, notify: Boolean = true) = if (notify) listener.onSave(settings) else Unit

    abstract fun default(): T
    abstract fun add(settings: T, notify: Boolean = true)
    abstract fun remove(settings: T, scope: ExecConnectionScope = settings.scope, notify: Boolean = true)

    fun save(settings: Collection<T>) = save(
        settings.groupBy { it.scope }
            .mapValues { (_, v) -> v.toList() }
    )

    fun save(settings: T) {
        remove(
            settings = settings,
            notify = false,
        )
        add(
            settings = settings,
            notify = false
        )

        onSave(mapOf(settings.scope to listOf(settings)))
    }

    fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        .findProperty(key)
        ?: fallback
}