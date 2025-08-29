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
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.exec.settings.event.ExecConnectionListener
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.project.PropertyService

abstract class ExecConnectionService<T : ExecConnectionSettingsState>(protected val project: Project) {

    abstract var activeConnection: T
    abstract val connections: List<T>

    protected abstract val listener: ExecConnectionListener<T>

    protected fun onActivate(settings: T, notify: Boolean = true) = if (notify) listener.onActive(settings) else Unit
    protected fun onRemove(settings: T, notify: Boolean = true) = if (notify) listener.onRemoved(settings) else Unit

    protected fun onAdd(settings: T, notify: Boolean = true) = if (notify) {
        saveCredentials(settings)
        listener.onAdded(settings)
    } else Unit

    protected fun onSave(settings: List<T>, notify: Boolean = true) {
        settings.forEach { saveCredentials(it) }
        if (notify) listener.onSave(settings) else Unit
    }

    abstract fun default(): T
    abstract fun remove(settings: T, notify: Boolean = true)
    abstract fun add(settings: T, notify: Boolean = true)

    fun save(settings: T) = save(listOf(settings))

    fun save(settings: List<T>) {
        settings.forEach { remove(it, notify = false) }
        settings.forEach { add(it, notify = false) }

        onSave(settings)
    }

    private fun saveCredentials(settings: T) {
        val credentials = settings.credentials
            ?: throw HybrisConfigurationException("Credentials must be set for Connection Settings.")

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Persisting credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes("SAP CX - ${settings.uuid}")
                PasswordSafe.instance.set(credentialAttributes, credentials)
            }
        })
    }

    protected fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        .findProperty(key)
        ?: fallback
}