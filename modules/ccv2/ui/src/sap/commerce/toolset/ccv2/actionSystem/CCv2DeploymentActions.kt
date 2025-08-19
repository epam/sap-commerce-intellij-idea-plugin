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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2DeploymentDto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab

class CCv2FetchDeploymentsAction : CCv2FetchAction<CCv2DeploymentDto>(
    tab = CCv2Tab.DEPLOYMENTS,
    text = "Fetch Deployments",
    icon = HybrisIcons.CCv2.Actions.FETCH,
    fetch = { project, subscriptions, onCompleteCallback ->
        CCv2Service.getInstance(project).fetchDeployments(subscriptions, onCompleteCallback)
    }
)

class CCv2TrackDeploymentAction(
    private val subscription: CCv2Subscription,
    private val deployment: CCv2DeploymentDto
) : CCv2Action(
    tab = CCv2Tab.DEPLOYMENTS,
    text = "Track Deployment",
    icon = HybrisIcons.CCv2.Deployment.Actions.WATCH
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        CCv2Service.getInstance(project).trackDeployment(project, subscription, deployment.code, deployment.buildCode)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        e.presentation.isEnabled = e.presentation.isEnabled && deployment.canTrack()
    }
}
