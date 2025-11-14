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

package sap.commerce.toolset.solr.exec.settings.state

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.MutableBooleanProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.util.xmlb.annotations.OptionTag
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.settings.state.ExecConnectionCredentials
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.solr.SolrConstants
import java.util.*

data class SolrConnectionSettingsState(
    @OptionTag override val uuid: String = UUID.randomUUID().toString(),
    @OptionTag override val scope: ExecConnectionScope = ExecConnectionScope.PROJECT_PERSONAL,
    @OptionTag override val name: String? = null,
    @OptionTag override val host: String = ExecConstants.DEFAULT_HOST_URL,
    @OptionTag override val port: String? = null,
    @OptionTag override val webroot: String = "solr",
    @OptionTag override val ssl: Boolean = true,
    @OptionTag override val timeout: Int = SolrConstants.CONNECTION_TIMEOUT_MILLIS,
    @OptionTag val socketTimeout: Int = SolrConstants.SOCKET_TIMEOUT_MILLIS,
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
        socketTimeout = socketTimeout,
    )

    data class Mutable(
        override var uuid: String = UUID.randomUUID().toString(),
        override var scope: ExecConnectionScope,
        override var name: ObservableMutableProperty<String>,
        override var host: ObservableMutableProperty<String>,
        override var port: ObservableMutableProperty<String>,
        override var webroot: ObservableMutableProperty<String>,
        override var ssl: MutableBooleanProperty,
        override var timeout: Int,
        override var modified: Boolean = false,
        override val username: ObservableMutableProperty<String> = AtomicProperty(""),
        override val password: ObservableMutableProperty<String> = AtomicProperty(""),
        override val proxyUsername: ObservableMutableProperty<String> = AtomicProperty(""),
        override val proxyPassword: ObservableMutableProperty<String> = AtomicProperty(""),
        var socketTimeout: Int,
    ) : ExecConnectionSettingsState.Mutable {

        override fun immutable() = SolrConnectionSettingsState(
            uuid = uuid,
            scope = scope,
            name = name.get(),
            host = host.get(),
            port = port.get(),
            webroot = webroot.get(),
            ssl = ssl.get(),
            timeout = timeout,
            socketTimeout = socketTimeout,
        ) to ExecConnectionCredentials(Credentials(username.get(), password.get()))
    }
}