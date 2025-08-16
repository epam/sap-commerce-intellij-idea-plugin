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
import sap.commerce.toolset.ccv2.model.CreateDeploymentRequestDTO
import sap.commerce.toolset.ccv2.model.DeploymentDetailDTO
import javax.swing.Icon

enum class CCv2DeploymentDatabaseUpdateModeEnum(val title: String, val icon: Icon, val apiMode: CreateDeploymentRequestDTO.DatabaseUpdateMode) {
    NONE("None", HybrisIcons.CCv2.Deployment.UpdateMode.NONE, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE),
    UPDATE("Update", HybrisIcons.CCv2.Deployment.UpdateMode.UPDATE, CreateDeploymentRequestDTO.DatabaseUpdateMode.UPDATE),
    INITIALIZE("Initialize", HybrisIcons.CCv2.Deployment.UpdateMode.INIT, CreateDeploymentRequestDTO.DatabaseUpdateMode.INITIALIZE),
    UNKNOWN("Unknown", HybrisIcons.CCv2.Deployment.UpdateMode.UNKNOWN, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN

        fun tryValueOf(mode: DeploymentDetailDTO.DatabaseUpdateMode?) = entries
            .find { it.name == mode?.name }
            ?: UNKNOWN

        fun allowedOptions() = listOf(NONE, UPDATE, INITIALIZE)
    }
}