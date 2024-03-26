/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.ccv2.dto

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import javax.swing.Icon

data class CCv2Environment(
    val code: String,
    val name: String,
    val type: CCv2EnvironmentType,
    val status: CCv2EnvironmentStatus,
    val deploymentStatus: CCv2EnvironmentDeploymentStatus,
) : CCv2DTO

enum class CCv2EnvironmentType(val title: String, val icon: Icon) {
    DEV("Development", HybrisIcons.TYPE_SYSTEM),
    STG("Staging", HybrisIcons.TYPE_SYSTEM),
    PROD("Production", HybrisIcons.TYPE_SYSTEM),
    UNKNOWN("Unknown", HybrisIcons.TYPE_SYSTEM);

    companion object {
        fun tryValueOf(name: String) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}

enum class CCv2EnvironmentStatus(val title: String, val icon: Icon) {
    PROVISIONING("Provisioning", HybrisIcons.CCV2_ENV_STATUS_PROVISIONING),
    AVAILABLE("Available", HybrisIcons.CCV2_ENV_STATUS_AVAILABLE),
    TERMINATING("Terminating", HybrisIcons.CCV2_ENV_STATUS_TERMINATING),
    TERMINATED("Terminated", HybrisIcons.CCV2_ENV_STATUS_TERMINATED),
    READY_FOR_DEPLOYMENT("Ready for deployment", HybrisIcons.CCV2_ENV_STATUS_READY_FOR_DEPLOYMENT),
    UNKNOWN("Unknown", HybrisIcons.CCV2_ENV_STATUS_UNKNOWN);

    companion object {
        fun tryValueOf(name: String) = CCv2EnvironmentStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}

enum class CCv2EnvironmentDeploymentStatus(val title: String, val icon: Icon) {
    SCHEDULED("Scheduled", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_SCHEDULED),
    DEPLOYING("Deploying", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_DEPLOYING),
    DEPLOYED("Deployed", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_DEPLOYED),
    UNDEPLOYED("Undeployed", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_UNDEPLOYED),
    FAIL("Fail", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_FAIL),
    UNKNOWN("Unknown", HybrisIcons.CCV2_ENV_DEPLOYMENT_STATUS_UNKNOWN);

    companion object {
        fun tryValueOf(name: String) = CCv2EnvironmentDeploymentStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}
