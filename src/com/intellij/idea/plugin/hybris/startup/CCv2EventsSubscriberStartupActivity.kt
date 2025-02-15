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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2BuildsListener
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentRequest
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CCv2EventsSubscriberStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        with(project.messageBus.connect()) {
            subscribe(CCv2Service.TOPIC_BUILDS, object : CCv2BuildsListener {
                override fun onBuildCompleted(subscription: CCv2Subscription, buildCode: String, deploymentRequests: Collection<CCv2DeploymentRequest>) {
                    val ccv2Service = CCv2Service.getInstance(project)

                    ccv2Service.fetchBuildWithCode(subscription, buildCode, {}) { build ->
                        if (build.canDeploy()) {
                            deploymentRequests
                                .forEach { ccv2Service.deployBuild(project, subscription, build, it) }
                        } else {
                            Notifications
                                .create(
                                    NotificationType.WARNING,
                                    "Build $buildCode could not be deployed.",
                                    "Build status: ${build.status}"
                                )
                                .hideAfter(15)
                                .notify(project)
                        }
                    }
                }
            })
        }
    }
}