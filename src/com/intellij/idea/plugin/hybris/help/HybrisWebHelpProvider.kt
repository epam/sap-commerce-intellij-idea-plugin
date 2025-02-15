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
package com.intellij.idea.plugin.hybris.help

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.help.WebHelpProvider

class HybrisWebHelpProvider : WebHelpProvider() {

    override fun getHelpPageUrl(helpTopicId: String) = when (helpTopicId) {
        CCV2_DEPLOYMENTS -> "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/106f7a8370db44a0b052f4a0cd5c4deb.html"
        else -> null
    }

    companion object {
        const val CCV2_DEPLOYMENTS = HybrisConstants.PLUGIN_ID + ".ccv2.deployments"
    }
}
