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

package sap.commerce.toolset.ccv2

import java.time.format.DateTimeFormatter

object CCv2Constants {
    const val DOMAIN = "portal.commerce.ondemand.com"

    const val MANIFEST_NAME = "manifest.json"
    const val CORE_CUSTOMIZE_NAME = "core-customize"
    const val DATAHUB_NAME = "datahub"
    const val JS_STOREFRONT_NAME = "js-storefront"

    const val SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN = "SAP CX CCv2 Token"

    val DATE_TIME_FORMATTER_LOCAL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss")

    val COMMERCE_EXTENSION_PACKS = arrayOf(
        "hybris-commerce-integrations",
        "hybris-datahub-integration-suite",
        "cx-commerce-crm-integrations",
        "media-telco"
    )

    val CLOUD_EXTENSIONS = arrayOf(
        "azurecloudhotfolder",
        "cloudmediaconversion",
        "cloudcommons",
        "cloudhotfolder",
        "cloudstorestorefront",
        "cloudstoreinitialdata",
        "cloudstorefulfilmentprocess",
        "cloudstorefacades",
        "cloudstorecore",
        "cloudstorecockpits"
    )
}