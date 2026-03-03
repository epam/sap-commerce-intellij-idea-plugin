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

package sap.commerce.toolset.ccv2.dto

enum class CCv2ScheduledActivityType(val title: String, val description: String) {
    UPGRADE_PREMIUM_DR(
        title = "Upgrade premium dr",
        description = "Changes the Disaster Recovery package from Standard to Premium."
    ),
    DOWNGRADE_TO_STANDARD_DR(
        title = "Downgrade to standard dr",
        description = "Changes the Disaster Recovery package back from Premium to Standard."
    ),
    HIBERNATE_COMMERCE_ENVIRONMENT(
        title = "Hibernate commerce environment",
        description = "If you have environments that are inactive, but you still want to keep them, you can place them in hibernation. This process archives the environment, and downgrades the databases and storage."
    ),
    WAKE_UP_COMMERCE_ENVIRONMENT(
        title = "Wake up commerce environment",
        description = "If you have any hibernated environments, you can re-activate them with this activity type. This process wakes up the environment, restoring its database and any related media."
    ),
    UNKNOWN(
        title = "Unknown",
        description = "Unknown scheduled activity type. Not yet supported by the Plugin."
    );

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}