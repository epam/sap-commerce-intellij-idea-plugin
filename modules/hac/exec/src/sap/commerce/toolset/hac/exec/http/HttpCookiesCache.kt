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

package sap.commerce.toolset.hac.exec.http

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.exec.settings.state.ConnectionSettingsState
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@Service(Service.Level.PROJECT)
class HttpCookiesCache(private val project: Project) : Disposable {

    private val cookiesPerSettings = ConcurrentHashMap<String, MutableMap<String, String>>()

    init {
        project.messageBus.connect().subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
            override fun onDelete(connection: HacConnectionSettingsState) = invalidateCookies(connection)
            override fun onUpdate(settings: Collection<HacConnectionSettingsState>) = settings.forEach { invalidateCookies(it) }
            override fun onSave(settings: Collection<HacConnectionSettingsState>) = settings.forEach { invalidateCookies(it) }
        })
    }

    override fun dispose() = cookiesPerSettings.clear()

    fun getKey(settings: ConnectionSettingsState, context: ReplicaContext? = null) = "${settings.uuid}_${context?.replicaId ?: "auto"}"

    private fun invalidateCookies(settings: ConnectionSettingsState) {
        val suitableCacheKeys = cookiesPerSettings.keys
            .filter { it.startsWith(settings.uuid) }
            .toList()

        suitableCacheKeys.forEach(Consumer { key: String? -> cookiesPerSettings.remove(key) })
    }

    companion object {
        fun getInstance(project: Project): HttpCookiesCache = project.service()
    }

}