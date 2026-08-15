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

package sap.commerce.toolset.exec.settings.state

import com.intellij.openapi.observable.properties.MutableBooleanProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import sap.commerce.toolset.exec.generateUrl
import sap.commerce.toolset.settings.state.MutableState
import sap.commerce.toolset.settings.state.Mutation

interface ExecConnectionSettingsState : ConnectionSettingsState {
    override val scope: ExecConnectionScope
    override val uuid: String
    override val name: String?
    override val ssl: Boolean
    override val host: String
    override val port: String?
    override val webroot: String
    override val timeout: Int

    fun mutable(): Mutable

    interface Snapshot<T : ExecConnectionSettingsState> {
        val state: T
        val mutation: Mutation
        val credentials: ExecCredentials
        val proxyCredentials: ExecCredentials
    }

    interface Mutable : MutableState {
        var scope: ExecConnectionScope
        var uuid: String
        var name: ObservableMutableProperty<String>
        var host: ObservableMutableProperty<String>
        var ssl: MutableBooleanProperty
        var timeout: Int
        var port: ObservableMutableProperty<String>
        var webroot: ObservableMutableProperty<String>
        val credentials: ExecCredentials.Mutable
        val proxyCredentials: ExecCredentials.Mutable

        val generatedURL: String
            get() = generateUrl(ssl.get(), host.get(), port.get(), webroot.get())

        val presentationName: String
            get() = connectionPresentationName(scope, name.get()) { generatedURL }

        fun snapshot(): Snapshot<out ExecConnectionSettingsState>
        fun copy(): Mutable
    }

}
