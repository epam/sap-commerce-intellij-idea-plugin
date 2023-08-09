package com.intellij.idea.plugin.hybris.tools.remote.action

import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesPanel
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

abstract class AbstractExecuteAction(title: String, description: String, icon: Icon) : AnAction(title, description, icon), DumbAware {

    protected abstract val extension: String
    protected abstract val consoleName: String
    protected open fun doExecute(consolePanel: HybrisConsolesPanel) {
        consolePanel.execute()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val project = e.project ?: return

        val selectionModel = editor.selectionModel
        var content = selectionModel.selectedText
        if (content == null || content.trim { it <= ' ' }.isEmpty()) {
            content = editor.document.text
        }

        val toolWindowService = HybrisToolWindowService.getInstance(project)
        with(toolWindowService) {
            this.activateToolWindow()
            this.activateToolWindowTab(HybrisToolWindowFactory.CONSOLES_ID)
        }

        val consolesPanel = toolWindowService.consolesPanel
        val console = consolesPanel.findConsole(consoleName)
        if (console == null) {
            LOG.warn("unable to find console $consoleName")
            return
        }
        consolesPanel.setActiveConsole(console)
        consolesPanel.sendTextToConsole(console, content)
        toolWindowService.activateToolWindow()
        doExecute(consolesPanel)
    }

    override fun update(e: AnActionEvent) {
        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && file.name.endsWith(this.extension)
        e.presentation.isEnabledAndVisible = enabled
    }

    companion object {
        private val LOG = Logger.getInstance(AbstractExecuteAction::class.java)
    }
}
