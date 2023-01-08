package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons.HYBRIS_REMOTE_ICON
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsConfigurableProvider.HybrisProjectSettingsConfigurable
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings.Type
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesToolWindow
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.options.ShowSettingsUtil

class HybrisChooseInstanceAction : AnAction(
    message("action.choose.hybris.instance.message.text"),
    message("action.choose.hybris.instance.message.title"),
    HYBRIS_REMOTE_ICON
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return

        ShowSettingsUtil.getInstance().showSettingsDialog(project, HybrisProjectSettingsConfigurable::class.java)
    }

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
