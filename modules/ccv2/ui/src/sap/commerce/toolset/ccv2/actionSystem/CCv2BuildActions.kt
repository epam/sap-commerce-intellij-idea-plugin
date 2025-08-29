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

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.HybrisIcons.CCv2.Actions.FETCH
import sap.commerce.toolset.HybrisIcons.CCv2.Build.Actions.SHOW_DETAILS
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2BuildDto
import sap.commerce.toolset.ccv2.dto.CCv2BuildRequest
import sap.commerce.toolset.ccv2.dto.CCv2BuildStatus
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SettingsState
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.toolwindow.CCv2ViewUtil
import sap.commerce.toolset.ccv2.ui.CCv2CreateBuildDialog
import sap.commerce.toolset.ccv2.ui.CCv2DeployBuildDialog

val subscriptionKey = DataKey.create<CCv2Subscription>("subscription")
val buildKey = DataKey.create<CCv2BuildDto>("build")

class CCv2CreateBuildAction : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Schedule Build",
    icon = HybrisIcons.CCv2.Build.Actions.CREATE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.dataContext.getData(subscriptionKey)
            ?: CCv2DeveloperSettings.getInstance(project).getActiveCCv2Subscription()
        val build = e.dataContext.getData(buildKey)

        CCv2CreateBuildDialog(project, subscription, build).showAndGet()
    }
}

class CCv2RedoBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Redo Build",
    icon = HybrisIcons.CCv2.Build.Actions.REDO
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        CCv2CreateBuildDialog(project, subscription, build).showAndGet()
    }
}

class CCv2DeployBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Deploy Build",
    icon = HybrisIcons.CCv2.Build.Actions.DEPLOY
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        CCv2DeployBuildDialog(project, subscription, build).showAndGet()
    }
}

class CCv2TrackBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Track Build",
    icon = HybrisIcons.CCv2.Build.Actions.WATCH
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val buildRequest = CCv2BuildRequest(subscription, build.name, build.branch, build.canTrack())
        CCv2Service.getInstance(project).trackBuild(project, buildRequest, build.code)
    }
}

class CCv2DeleteBuildAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Delete Build",
    icon = HybrisIcons.CCv2.Build.Actions.DELETE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (Messages.showYesNoDialog(
                project,
                """
                    Are you certain that you want to delete build '${build.code}' within the '$subscription' subscription?<br>
                    The build will be deleted permanently in 14 day(s).<br>
                    During this period you can request a restore via ticket to your system administrator.
                """.trimIndent(),
                "Delete CCv2 Build",
                HybrisIcons.CCv2.Build.Actions.DELETE
            ) != Messages.YES
        ) return

        CCv2Service.getInstance(project).deleteBuild(project, subscription, build)
    }
}

class CCv2FetchBuildsAction : CCv2FetchAction<CCv2BuildDto>(
    tab = CCv2Tab.BUILDS,
    text = "Fetch Builds",
    icon = FETCH,
    fetch = { project, subscriptions, onCompleteCallback ->
        CCv2Service.getInstance(project).fetchBuilds(subscriptions, onCompleteCallback)
    }
)

class CCv2FetchBuildDetailsAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto,
    private val onCompleteCallback: (CCv2BuildDto) -> Unit
) : DumbAwareAction("Fetch Build", null, FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        fetching = true

        CCv2Service.getInstance(project).fetchBuildWithCode(
            subscription, build.code,
            { response ->
                fetching = false

                invokeLater {
                    onCompleteCallback.invoke(response)
                }
            }
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !fetching
        e.presentation.text = if (fetching) "Fetching" else "Fetch Build"
        e.presentation.disabledIcon = if (fetching) AnimatedIcon.Default.INSTANCE else FETCH
    }
}

class CCv2DownloadBuildLogsAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Download Build Logs",
    icon = HybrisIcons.CCv2.Build.Actions.DOWNLOAD_LOGS
) {
    private var fetching = false

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        fetching = true

        CCv2Service.getInstance(project).downloadBuildLogs(project, subscription, build, onCompleteCallback(project))
    }

    private fun onCompleteCallback(project: Project): (Collection<VirtualFile>) -> Unit = {
        fetching = false

        invokeLater {
            it.forEach {
                FileEditorManager.getInstance(project).openFile(it, true)
            }
        }
    }

    override fun isEnabled() = !fetching && super.isEnabled()
}

class CCv2ShowBuildDetailsAction(
    private val subscription: CCv2Subscription,
    private val build: CCv2BuildDto
) : DumbAwareAction("Show Build Details", null, SHOW_DETAILS) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CCv2ViewUtil.showBuildDetailsTab(project, subscription, build)
    }

}

abstract class CCv2ShowBuildWithStatusAction(status: CCv2BuildStatus) : CCv2ShowWithStatusAction<CCv2BuildStatus>(
    CCv2Tab.BUILDS,
    status,
    status.title,
    status.icon
) {

    override fun getStatuses(settings: CCv2SettingsState) = settings.showBuildStatuses

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val ccv2DeveloperSettings = CCv2DeveloperSettings.getInstance(project)
        val mutable = ccv2DeveloperSettings.ccv2Settings.mutable()
        if (state) mutable.showBuildStatuses.add(status)
        else mutable.showBuildStatuses.remove(status)

        ccv2DeveloperSettings.ccv2Settings = mutable.immutable()
    }
}

class CCv2ShowDeletedBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.DELETED)
class CCv2ShowFailedBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.FAIL)
class CCv2ShowUnknownBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.UNKNOWN)
class CCv2ShowScheduledBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.SCHEDULED)
class CCv2ShowBuildingBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.BUILDING)
class CCv2ShowSuccessBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.SUCCESS)
