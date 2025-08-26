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

package sap.commerce.toolset.hac.exec.settings.state

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.hac.HacConstants
import java.util.*

data class HacConnectionSettingsState(
    @OptionTag override val uuid: String = UUID.randomUUID().toString(),

    @OptionTag override val scope: ExecConnectionScope = ExecConnectionScope.PROJECT_PERSONAL,
    @OptionTag override val name: String? = null,
    @OptionTag override val host: String = ExecConstants.DEFAULT_HOST_URL,
    @OptionTag override val port: String? = null,
    @OptionTag override val webroot: String = "",
    @OptionTag override val ssl: Boolean = true,
    @OptionTag override val timeout: Int = HacConstants.DEFAULT_TIMEOUT,

    @Transient
    override val credentials: Credentials? = null,

    @JvmField @OptionTag val wsl: Boolean = false,
    @JvmField @OptionTag val sslProtocol: String = "TLSv1.2",
    @JvmField @OptionTag val sessionCookieName: String = ExecConstants.DEFAULT_SESSION_COOKIE_NAME,
) : ExecConnectionSettingsState {

    private val dynamicCredentials
        @Transient
        get() = credentials
            ?: PasswordSafe.instance.get(CredentialAttributes("SAP CX - $uuid"))
    override val username
        @Transient
        get() = dynamicCredentials?.userName ?: DEFAULT_USERNAME
    override val password
        @Transient
        get() = dynamicCredentials?.getPasswordAsString() ?: DEFAULT_PASSWORD

    override fun mutable() = Mutable(
        uuid = uuid,
        scope = scope,
        name = name,
        host = host,
        port = port,
        webroot = webroot,
        ssl = ssl,
        timeout = timeout,
        wsl = wsl,
        sslProtocol = sslProtocol,
        sessionCookieName = sessionCookieName
    )

    data class Mutable(
        override var uuid: String = UUID.randomUUID().toString(),
        override var scope: ExecConnectionScope,
        override var name: String?,
        override var host: String,
        override var port: String?,
        override var webroot: String,
        override var ssl: Boolean,
        override var timeout: Int,
        var wsl: Boolean,
        var sslProtocol: String,
        var sessionCookieName: String,
    ) : ExecConnectionSettingsState.Mutable {

        override val username
            get() = PasswordSafe.instance.get(CredentialAttributes("SAP CX - $uuid"))
                ?.userName
                ?: DEFAULT_USERNAME
        override val password
            get() = PasswordSafe.instance.get(CredentialAttributes("SAP CX - $uuid"))
                ?.getPasswordAsString()
                ?: DEFAULT_PASSWORD

        override fun immutable() = HacConnectionSettingsState(
            uuid = uuid,
            scope = scope,
            name = name,
            host = host,
            port = port,
            webroot = webroot,
            ssl = ssl,
            timeout = timeout,
            wsl = wsl,
            sslProtocol = sslProtocol,
            sessionCookieName = sessionCookieName
        )
    }

    companion object {
        private const val DEFAULT_USERNAME = "admin"
        private const val DEFAULT_PASSWORD = "nimda"
    }
}