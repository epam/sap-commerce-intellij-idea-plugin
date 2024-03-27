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

package com.intellij.idea.plugin.hybris.tools.ccv2.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.CCv2CreateBuildDialog
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.ui.Messages

val subscriptionKey = DataKey.create<CCv2Subscription>("subscription")
val buildKey = DataKey.create<CCv2Build>("build")

class CCv2CreateBuildAction : AbstractCCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Schedule a Build",
    icon = HybrisIcons.CCV2_BUILD_CREATE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val subscription = e.dataContext.getData(subscriptionKey)
        val build = e.dataContext.getData(buildKey)
        val project = e.project ?: return

        CCv2CreateBuildDialog(project, subscription, build).showAndGet()
    }
}

class CCv2RedoBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2Build
) : AbstractCCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Redo a Build",
    icon = HybrisIcons.CCV2_BUILD_REDO
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        CCv2CreateBuildDialog(project, subscription, build).showAndGet()
    }
}

class CCv2DeleteBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2Build
) : AbstractCCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Delete the Build",
    icon = HybrisIcons.CCV2_BUILD_DELETE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (Messages.showYesNoDialog(
                project,
                "Are you sure you want to delete '${build.code}' build within the '$subscription' subscription?",
                "Delete CCv2 Build",
                HybrisIcons.CCV2_BUILD_DELETE
            ) != Messages.YES
        ) return

        CCv2Service.getInstance(project).deleteBuild(project, subscription, build)
    }
}

class CCv2FetchBuildsAction : AbstractCCv2FetchAction(
    tab = CCv2Tab.BUILDS,
    text = "Fetch Builds",
    icon = HybrisIcons.CCV2_FETCH,
    fetch = { project, subscriptions, onStart, onComplete -> CCv2Service.getInstance(project).fetchBuilds(subscriptions, onStart, onComplete) }
)