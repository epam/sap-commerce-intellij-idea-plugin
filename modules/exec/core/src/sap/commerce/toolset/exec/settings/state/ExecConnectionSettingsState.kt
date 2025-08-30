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

package sap.commerce.toolset.exec.settings.state

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.observable.properties.ObservableMutableProperty

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

    interface Mutable : ConnectionSettingsState {
        override var scope: ExecConnectionScope
        override var uuid: String
        override var name: String?
        override var ssl: Boolean
        override var timeout: Int
        override var host: String
        override var port: String?
        override var webroot: String
        var modified: Boolean
        val username: ObservableMutableProperty<String>
        val password: ObservableMutableProperty<String>

        fun immutable(): Pair<ExecConnectionSettingsState, Credentials>
    }
}
