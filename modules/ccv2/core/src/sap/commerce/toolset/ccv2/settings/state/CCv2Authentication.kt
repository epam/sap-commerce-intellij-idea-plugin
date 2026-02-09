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

package sap.commerce.toolset.ccv2.settings.state

import com.intellij.credentialStore.Credentials
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag
import sap.commerce.toolset.ccv2.CCv2Constants

// See: https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/edcfd89aa5154be59910ebb7081030e3.html
@Tag("authentication")
data class CCv2Authentication(
    @JvmField @OptionTag val tokenEndpoint: String = CCv2Constants.Authentication.TOKEN_ENDPOINT,
    @JvmField @OptionTag val resource: String = CCv2Constants.Authentication.RESOURCE,
) {

    fun mutable() = Mutable(
        tokenEndpoint = tokenEndpoint,
        resource = resource,
    )

    data class Mutable(
        var tokenEndpoint: String = CCv2Constants.Authentication.TOKEN_ENDPOINT,
        var resource: String = CCv2Constants.Authentication.RESOURCE,
        var clientId: String = "",
        var clientSecret: String = "",
    ) {
        val credentials: Credentials?
            get() = Credentials(clientId, clientSecret)
                .takeUnless { it.userName.isNullOrBlank() || it.password.isNullOrBlank() }

        fun immutable() = CCv2Authentication(
            tokenEndpoint = tokenEndpoint,
            resource = resource,
        )
    }
}
