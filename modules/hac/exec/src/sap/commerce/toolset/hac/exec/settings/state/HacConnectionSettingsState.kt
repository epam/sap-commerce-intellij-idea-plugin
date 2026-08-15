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

package sap.commerce.toolset.hac.exec.settings.state

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.MutableBooleanProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.util.xmlb.annotations.OptionTag
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.ExecCredentials
import sap.commerce.toolset.hac.HacExecConstants
import sap.commerce.toolset.settings.state.Mutation
import java.util.*

data class HacConnectionSettingsState(
    @OptionTag override val uuid: String = UUID.randomUUID().toString(),

    @OptionTag override val scope: ExecConnectionScope = ExecConnectionScope.PROJECT_PERSONAL,
    @OptionTag override val name: String? = null,
    @OptionTag override val host: String = ExecConstants.DEFAULT_HOST_URL,
    @OptionTag override val port: String? = null,
    @OptionTag override val webroot: String = "",
    @OptionTag override val ssl: Boolean = true,
    @OptionTag override val timeout: Int = HacExecConstants.DEFAULT_TIMEOUT,

    @JvmField @OptionTag val wsl: Boolean = false,
    @JvmField @OptionTag val sslProtocol: String = "TLSv1.2",
    @JvmField @OptionTag val sessionCookieName: String = "",
    @JvmField @OptionTag val authMode: AuthMode = AuthMode.AUTOMATIC,
    @JvmField @OptionTag val proxyAuthMode: ProxyAuthMode = ProxyAuthMode.NONE,
) : ExecConnectionSettingsState {

    override fun mutable() = Mutable(
        uuid = uuid,
        scope = scope,
        name = AtomicProperty(name ?: ""),
        host = AtomicProperty(host),
        port = AtomicProperty(port ?: ""),
        webroot = AtomicProperty(webroot),
        ssl = AtomicBooleanProperty(ssl),
        timeout = timeout,
        wsl = AtomicBooleanProperty(wsl),
        sslProtocol = AtomicProperty(sslProtocol),
        sessionCookieName = sessionCookieName,
        proxyAuthMode = AtomicProperty(proxyAuthMode),
        authMode = AtomicProperty(authMode),
    )

    data class Mutable(
        override var mutation: Mutation = Mutation.NONE,
        override var uuid: String = UUID.randomUUID().toString(),
        override var scope: ExecConnectionScope,
        override var name: ObservableMutableProperty<String>,
        override var host: ObservableMutableProperty<String>,
        override var port: ObservableMutableProperty<String>,
        override var webroot: ObservableMutableProperty<String>,
        override var ssl: MutableBooleanProperty,
        override var timeout: Int,
        override val credentials: ExecCredentials.Mutable = ExecCredentials.Mutable(),
        override val proxyCredentials: ExecCredentials.Mutable = ExecCredentials.Mutable(),
        val authMode: ObservableMutableProperty<AuthMode>,
        val wsl: ObservableMutableProperty<Boolean>,
        val proxyAuthMode: ObservableMutableProperty<ProxyAuthMode>,
        var sslProtocol: ObservableMutableProperty<String>,
        var sessionCookieName: String,
    ) : ExecConnectionSettingsState.Mutable {

        override fun snapshot() = Snapshot(
            state = state(),
            mutation = mutation,
            credentials = credentials.immutable(),
            proxyCredentials = proxyCredentials.immutable(),
        )

        override fun copy(): Mutable = snapshot().state.mutable()
            .also {
                it.credentials.load(credentials)
                it.proxyCredentials.load(proxyCredentials)
            }

        private fun state(): HacConnectionSettingsState = HacConnectionSettingsState(
            uuid = uuid,
            scope = scope,
            name = name.get(),
            host = host.get(),
            port = port.get(),
            webroot = webroot.get(),
            ssl = ssl.get(),
            timeout = timeout,
            wsl = wsl.get(),
            sslProtocol = sslProtocol.get(),
            sessionCookieName = sessionCookieName,
            proxyAuthMode = proxyAuthMode.get(),
            authMode = authMode.get(),
        )
    }

    data class Snapshot(
        override val state: HacConnectionSettingsState,
        override val mutation: Mutation,
        override val credentials: ExecCredentials,
        override val proxyCredentials: ExecCredentials,
    ) : ExecConnectionSettingsState.Snapshot<HacConnectionSettingsState>
}