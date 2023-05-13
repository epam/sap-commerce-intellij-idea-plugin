package com.intellij.idea.plugin.hybris.impex.file.actions;

import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.actions.CopyFileToHybrisConsoleUtils
import com.intellij.idea.plugin.hybris.actions.CopyFileToHybrisConsoleUtils.isRequiredMultipleFileExtension
import com.intellij.idea.plugin.hybris.common.HybrisConstants.IMPEX_CONSOLE_TITLE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.IMPEX_FILE_EXTENSION
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class CopyImpexFileAction : AnAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        event.presentation.isEnabledAndVisible = ActionUtils.isHybrisContext(project) && isRequiredMultipleFileExtension(project, IMPEX_FILE_EXTENSION)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        CopyFileToHybrisConsoleUtils.copySelectedFilesToHybrisConsole(project, IMPEX_CONSOLE_TITLE, IMPEX_FILE_EXTENSION);
    }
}
