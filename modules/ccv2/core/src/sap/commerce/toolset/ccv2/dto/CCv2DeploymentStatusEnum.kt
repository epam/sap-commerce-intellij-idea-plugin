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

package sap.commerce.toolset.ccv2.dto

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class CCv2DeploymentStatusEnum(val title: String, val icon: Icon) {
    SCHEDULED("Scheduled", HybrisIcons.CCv2.Deployment.Status.SCHEDULED),
    DEPLOYED("Deployed", HybrisIcons.CCv2.Deployment.Status.DEPLOYED),
    DEPLOYING("Deploying", HybrisIcons.CCv2.Deployment.Status.DEPLOYING),
    UNDEPLOYED("Undeployed", HybrisIcons.CCv2.Deployment.Status.UNDEPLOYED),
    FAIL("Fail", HybrisIcons.CCv2.Deployment.Status.FAIL),
    UNKNOWN("Unknown", HybrisIcons.CCv2.Deployment.Status.UNKNOWN);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}
