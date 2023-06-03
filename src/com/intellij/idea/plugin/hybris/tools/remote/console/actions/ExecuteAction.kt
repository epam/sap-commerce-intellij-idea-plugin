package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.HybrisConsoleExecuteActionHandler
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisTabs
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.EmptyAction
import com.intellij.openapi.project.DumbAwareAction
import javax.swing.Icon

abstract class HybrisExecuteActionBase(val executeActionHandler: HybrisConsoleExecuteActionHandler,
                                       icon: Icon) : AnAction(null, null, icon) {

    init {
        EmptyAction.setupAction(this, "Console.Execute.Immediately", null)
    }
}

class HybrisExecuteImmediatelyAction(private val tabbedPane: HybrisTabs, executeActionHandler: HybrisConsoleExecuteActionHandler) : 
        HybrisExecuteActionBase(executeActionHandler, AllIcons.Actions.Execute) {
    override fun actionPerformed(e: AnActionEvent) {
        executeActionHandler.runExecuteAction(tabbedPane)
    }
    override fun update(e: AnActionEvent) {
        val editor = tabbedPane.activeConsole().consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)
        e.presentation.isEnabled = !executeActionHandler.isProcessRunning && (lookup == null || !lookup.isCompletion)
    }
}

class HybrisSuspendAction(private val tabbedPane: HybrisTabs, executeActionHandler: HybrisConsoleExecuteActionHandler) :
        HybrisExecuteActionBase(executeActionHandler, AllIcons.Actions.Suspend) {
    override fun actionPerformed(e: AnActionEvent) {
        
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = executeActionHandler.isProcessRunning
    }

}