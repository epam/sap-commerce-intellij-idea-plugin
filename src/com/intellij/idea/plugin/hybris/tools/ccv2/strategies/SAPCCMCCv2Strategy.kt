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

package com.intellij.idea.plugin.hybris.tools.ccv2.strategies

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccm.SAPCCM
import com.intellij.idea.plugin.hybris.tools.ccm.SAPCCMBuildCommands
import com.intellij.idea.plugin.hybris.tools.ccm.SAPCCMDeploymentCommands
import com.intellij.idea.plugin.hybris.tools.ccm.SAPCCMEnvironmentCommands
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Deployment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

@Service
class SAPCCMCCv2Strategy : CCv2Strategy {

    override fun fetchEnvironments(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Environment>> {
        val appSettings = ApplicationSettingsComponent.getInstance()
        authCredentials(project, appSettings, ccv2Token) ?: return emptyMap()

        return SAPCCMEnvironmentCommands.list(project, appSettings, subscriptions)
    }

    override fun fetchBuilds(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Build>> {
        val appSettings = ApplicationSettingsComponent.getInstance()
        authCredentials(project, appSettings, ccv2Token) ?: return emptyMap()

        return SAPCCMBuildCommands.list(project, appSettings, subscriptions)
    }

    override fun fetchDeployments(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Deployment>> {
        val appSettings = ApplicationSettingsComponent.getInstance()
        authCredentials(project, appSettings, ccv2Token) ?: return emptyMap()

        return SAPCCMDeploymentCommands.list(project, appSettings, subscriptions)
    }

    override fun createBuild(project: Project, ccv2Token: String, subscription: CCv2Subscription, name: String, branch: String): CCv2Build? {
        val appSettings = ApplicationSettingsComponent.getInstance()
        authCredentials(project, appSettings, ccv2Token) ?: return null

        return SAPCCMBuildCommands.create(project, appSettings, subscription, name, branch)
    }

    override fun deleteBuild(project: Project, ccv2Token: String, subscription: CCv2Subscription, build: CCv2Build) {
        val appSettings = ApplicationSettingsComponent.getInstance()
        authCredentials(project, appSettings, ccv2Token) ?: return

        SAPCCMBuildCommands.delete(project, appSettings, subscription, build)
    }

    private fun authCredentials(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        ccv2Token: String
    ): List<String>? {
        ProgressManager.getInstance().progressIndicator.text2 = "Authenticating with the provided token..."
        return SAPCCM.execute(project, appSettings, "config", "set", "auth-credentials", ccv2Token)
    }

}