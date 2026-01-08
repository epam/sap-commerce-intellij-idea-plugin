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

package sap.commerce.toolset.java

object JavaConstants {

    object ModuleLibrary {
        const val BACKOFFICE = "backoffice"
        const val BACKOFFICE_TEST = "backoffice test"
        const val TEST = "test"
        const val WEB = "web"
        const val ADDON = "addon"
        const val ADDON_TEST = "addon test"
        const val EXTENSION = "extension"
        const val CONFIG_LICENSE = "config license"
    }

    object ProjectLibrary {
        const val PLATFORM_BOOTSTRAP = "Platform Bootstrap"
        const val DATABASE_DRIVERS = "Database Drivers"
        const val BACKOFFICE = "Backoffice Library"
    }
}