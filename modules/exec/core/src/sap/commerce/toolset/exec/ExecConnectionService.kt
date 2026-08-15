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
import sap.commerce.toolset.exec.settings.state.ExecCredentials
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.settings.state.Mutation

abstract class ExecConnectionService<T : ExecConnectionSettingsState, S : ExecConnectionSettingsState.Snapshot<T>>(protected val project: Project) {

    abstract var activeConnection: T
    abstract val connections: List<T>

    protected abstract val listener: ExecConnectionListener<T>

    abstract fun defaultCredentials(): Credentials
    abstract fun default(): T

    abstract fun save(snapshots: Collection<S>)
    abstract fun delete(state: T)
    abstract fun create(snapshot: S, notify: Boolean = true)

    fun getCredentials(uuid: String) = PasswordSafe.instance[CredentialAttributes("SAP CX - $uuid")]
        ?: defaultCredentials()

    fun getProxyCredentials(uuid: String) = PasswordSafe.instance[CredentialAttributes("SAP CX - proxy - $uuid")]

    fun update(snapshot: S) {
        delete(snapshot.state)
        create(snapshot, false)
        onSave(listOf(snapshot))
    }

    protected fun onActivate(state: T, notify: Boolean = true) = if (notify) listener.onActivate(state) else Unit

    protected fun onCreate(snapshot: S, notify: Boolean = true) = if (notify) {
        saveCredentials(snapshot)
        listener.onCreate(snapshot.state)
    } else Unit

    protected fun onSave(snapshots: Collection<S>, notify: Boolean = true) {
        snapshots
            .filter { it.mutation == Mutation.SAVE }
            .forEach { saveCredentials(it) }
        if (notify) listener.onSave(snapshots.map { it.state })
    }

    protected fun removeCredentials(state: T) = writeCredentials(
        title = "Removing credentials",
        uuid = state.uuid,
    )

    private fun saveCredentials(snapshot: S) = writeCredentials(
        title = "Persisting credentials",
        uuid = snapshot.state.uuid,
        credentials = snapshot.credentials,
        proxyCredentials = snapshot.proxyCredentials,
    )

    private fun writeCredentials(title: String, uuid: String, credentials: ExecCredentials? = null, proxyCredentials: ExecCredentials? = null) = ProgressManager.getInstance()
        .run(object : Task.Backgroundable(project, title, false) {
            override fun run(indicator: ProgressIndicator) {
                credentials.write("SAP CX - $uuid")
                proxyCredentials.write("SAP CX - proxy - $uuid")
            }
        })

    private fun ExecCredentials?.write(serviceName: String) {
        if (this == null || this.mutation == Mutation.SAVE) {
            val credentialAttributes = CredentialAttributes(serviceName)
            PasswordSafe.instance[credentialAttributes] = this?.credentials
        }
    }

    protected fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        .findProperty(key)
        ?: fallback
}