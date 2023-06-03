package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.icons.AllIcons.Actions.Checked
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.HybrisConsoleExecuteValidateActionHandler
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisTabs
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class HybrisImpexValidateAction(private val tabbedPane: HybrisTabs,
                                private val executeValidationActionHandler: HybrisConsoleExecuteValidateActionHandler) : AnAction(
        HybrisI18NBundleUtils.message("action.console.hybris.impex.validate.message.text"),
        HybrisI18NBundleUtils.message("action.console.hybris.impex.validate.message.title"),
        Checked) {

    override fun actionPerformed(e: AnActionEvent) {
        executeValidationActionHandler.runExecuteAction(tabbedPane)
    }

    override fun update(e: AnActionEvent) {
        val editor = tabbedPane.activeConsole().consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)
        
        e.presentation.isEnabled = !executeValidationActionHandler.isProcessRunning && 
                (lookup == null || !lookup.isCompletion) && tabbedPane.activeConsole() is HybrisImpexConsole

        e.presentation.isVisible = tabbedPane.activeConsole() is HybrisImpexConsole
    }

}