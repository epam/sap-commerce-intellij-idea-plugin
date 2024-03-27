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
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

interface CCv2Strategy {

    fun fetchEnvironments(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Environment>>
    fun fetchBuilds(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Build>>
    fun createBuild(project: Project, ccv2Token: String, subscription: CCv2Subscription, name: String, branch: String): CCv2Build?
    fun deleteBuild(project: Project, ccv2Token: String, subscription: CCv2Subscription, build: CCv2Build)

    companion object {
        fun getSAPCCMCCv2Strategy(): SAPCCMCCv2Strategy = ApplicationManager.getApplication().getService(SAPCCMCCv2Strategy::class.java)
    }
}