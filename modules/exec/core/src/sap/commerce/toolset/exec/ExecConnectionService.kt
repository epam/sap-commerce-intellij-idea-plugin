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

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import sap.commerce.toolset.exec.settings.event.ExecConnectionListener
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.project.PropertyService

abstract class ExecConnectionService<T : ExecConnectionSettingsState>(protected val project: Project) {

    abstract var activeConnection: T
    abstract val connections: List<T>

    protected abstract val listener: ExecConnectionListener<T>

    abstract fun defaultCredentials(settings: T): Credentials
    abstract fun default(): T
    abstract fun delete(settings: T, notify: Boolean = true)
    abstract fun create(settings: Pair<T, Credentials>, notify: Boolean = true)
    abstract fun save(settings: Map<T, Credentials>)

    fun getCredentials(settings: T) = PasswordSafe.instance.get(CredentialAttributes("SAP CX - ${settings.uuid}"))
        ?: defaultCredentials(settings)

    fun update(settings: Pair<T, Credentials>) = update(mapOf(settings))

    fun update(settings: Map<T, Credentials>) {
        settings.keys.forEach { delete(it, notify = false) }
        settings.forEach { create(it.key to it.value, notify = false) }

        onUpdate(settings)
    }

    protected fun removeCredentials(settings: T) = saveCredentials(settings to null)

    protected fun onActivate(settings: T, notify: Boolean = true) = if (notify) listener.onActive(settings) else Unit
    protected fun onDelete(settings: T, notify: Boolean = true) {
        removeCredentials(settings)
        if (notify) listener.onDelete(settings) else Unit
    }

    protected fun onCreate(settings: Pair<T, Credentials>, notify: Boolean = true) = if (notify) {
        saveCredentials(settings)
        listener.onCreate(settings.first)
    } else Unit

    protected fun onUpdate(settings: Map<T, Credentials>, notify: Boolean = true) {
        settings.forEach { saveCredentials(it.key to it.value) }
        if (notify) listener.onUpdate(settings.keys)
    }

    protected fun onSave(settings: Map<T, Credentials>) {
        settings.forEach { saveCredentials(it.key to it.value) }
        listener.onSave(settings.keys)
    }

    private fun saveCredentials(settings: Pair<T, Credentials?>) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Persisting credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes("SAP CX - ${settings.first.uuid}")
                PasswordSafe.instance.set(credentialAttributes, settings.second)
            }
        })
    }

    protected fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        .findProperty(key)
        ?: fallback
}