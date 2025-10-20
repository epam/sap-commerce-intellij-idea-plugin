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

import com.intellij.openapi.actionSystem.DataKey
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.view.CCv2BuildDetailsView

object CCv2UiConstants {

    object DataKeys {
        val Subscription = DataKey.create<CCv2Subscription>("CCv2_Subscription")

        val Environment = DataKey.create<CCv2EnvironmentDto>("CCv2_Environment")
        val EnvironmentCallback = DataKey.create<(CCv2EnvironmentDto) -> Unit>("CCv2_EnvironmentCallback")

        val Service = DataKey.create<CCv2ServiceDto>("CCv2_Service")
        val ServiceReplica = DataKey.create<CCv2ServiceReplicaDto>("CCv2_ServiceReplica")
        val ServiceCallback = DataKey.create<(CCv2ServiceDto) -> Unit>("CCv2_ServiceCallback")

        val Build = DataKey.create<CCv2BuildDto>("CCv2_Build")
        val BuildDetailsView = DataKey.create<CCv2BuildDetailsView>("CCv2_BuildDetailsView")
        val BuildCallback = DataKey.create<(CCv2BuildDto) -> Unit>("CCv2_BuildCallback")

        val Deployment = DataKey.create<CCv2DeploymentDto>("CCv2_Deployment")
    }
}