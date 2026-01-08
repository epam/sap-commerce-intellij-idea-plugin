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

package sap.commerce.toolset.extensioninfo

object EiConstants {

    const val ROOT_TAG_EXTENSION_INFO_XML = "extensioninfo"

    const val EXTENSION_INFO_XML = "extensioninfo.xml"
    const val EXTENSION_META_KEY_BACKOFFICE_MODULE = "backoffice-module"
    const val EXTENSION_META_KEY_HAC_MODULE = "hac-module"
    const val EXTENSION_META_KEY_CLASSPATHGEN = "classpathgen"
    const val EXTENSION_META_KEY_DEPRECATED = "deprecated"
    const val EXTENSION_META_KEY_EXT_GEN = "extgen-template-extension"
    const val EXTENSION_META_KEY_MODULE_GEN = "modulegen-name"

    val EXTENSION_INFO_META_KEYS = listOf(
        EXTENSION_META_KEY_BACKOFFICE_MODULE,
        EXTENSION_META_KEY_HAC_MODULE,
        EXTENSION_META_KEY_CLASSPATHGEN,
        EXTENSION_META_KEY_DEPRECATED,
        EXTENSION_META_KEY_EXT_GEN,
        EXTENSION_META_KEY_MODULE_GEN
    )

    object Extension {
        @Deprecated("Introduce new BackofficeModuleDescriptor")
        const val BACK_OFFICE = "backoffice"
        const val CORE = "core"
        const val CONFIG = "config"
        @Deprecated("Introduce new HmcModuleDescriptor")
        const val HMC = "hmc"
        @Deprecated("Introduce new HacModuleDescriptor")
        const val HAC = "hac"
        const val PLATFORM = "platform"
        const val PLATFORM_SERVICES = "platformservices"
        const val ADDON_SUPPORT = "addonsupport"
        const val KOTLIN_NATURE = "kotlinnature"
        const val COMMON_WEB = "commonweb"
        const val WEB = "web"
        const val ADVANCED_SAVED_QUERY = "advancedsavedquery"
        const val CATALOG = "catalog"
        const val COMMENTS = "comments"
        const val COMMONS = "commons"
        const val DELIVERY_ZONE = "deliveryzone"
        const val EUROPE1 = "europe1"
        const val IMPEX = "impex"
        const val MAINTENANCE_WEB = "maintenanceweb"
        const val MEDIA_WEB = "mediaweb"
        const val OAUTH2 = "oauth2"
        const val PAYMENT_STANDARD = "paymentstandard"
        const val PROCESSING = "processing"
        const val SCRIPTING = "scripting"
        const val TEST_WEB = "testweb"
        const val VALIDATION = "validation"
        const val WORKFLOW = "workflow"
    }

}