package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons.HYBRIS_REMOTE_ICON
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsListener
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings.Type.SOLR
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesToolWindow
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages


class HybrisChooseInstanceAction : AnAction(
        message("action.choose.hybris.instance.message.text"),
        message("action.choose.hybris.instance.message.title"),
        HYBRIS_REMOTE_ICON) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val state = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state ?: return
        val list = state.remoteConnectionSettingsList
        val consoleType = HybrisConsolesToolWindow.getInstance(project).consolesPanel.getActiveConsole().connectionType()
        val currentList = list.filter {it.type == consoleType}.toList()
        val options:Array<String> = currentList.stream().map { it.toString() }.toArray<String> { length -> arrayOfNulls(length)}
        val active = if (consoleType == SOLR) {
            HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).getActiveSolrConnectionSettings(project)
        } else {
            HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).getActiveHybrisRemoteConnectionSettings(project)
        }
        val ret =  Messages.showChooseDialog(project,
                message("action.choose.hybris.instance.message.text"),
                message("action.choose.hybris.instance.message.title"),
                HYBRIS_REMOTE_ICON, options, active.toString())
        if (ret == -1)
            return
        if (consoleType == SOLR) {
            state.activeSolrConnectionID= currentList[ret].uuid
            project.messageBus.syncPublisher(HybrisDeveloperSpecificProjectSettingsListener.TOPIC).solrConnectionSettingsChanged()
        } else {
            state.activeRemoteConnectionID= currentList[ret].uuid
        }

    }

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        if (project == null) {
            e.presentation.isEnabledAndVisible=false
            return
        }
        val state = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
        if (state == null) {
            e.presentation.isEnabledAndVisible=false
            return
        }
        e.presentation.isEnabledAndVisible = state.remoteConnectionSettingsList.size > 1
    }
}
