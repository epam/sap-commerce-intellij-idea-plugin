package com.intellij.idea.plugin.hybris.tools.remote.action;

import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesPanel;
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractExecuteAction extends AnAction implements DumbAware  {

    private static final Logger LOG = Logger.getInstance(AbstractExecuteAction.class);

    protected abstract String getExtension();

    protected abstract String getConsoleName();

    protected void doExecute(final HybrisConsolesPanel consolePanel) {
        consolePanel.execute();
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        final var project = e.getProject();
        if (editor != null && project != null) {
            final SelectionModel selectionModel = editor.getSelectionModel();
            String content = selectionModel.getSelectedText();
            if (content == null || content.trim().isEmpty()) {
                content = editor.getDocument().getText();
            }

            final var consolesPanel = HybrisToolWindowService.Companion.getInstance(project).getConsolesPanel();
            final var console = consolesPanel.findConsole(getConsoleName());
            if (console == null) {
                LOG.warn("unable to find console " + getConsoleName());
                return;
            }
            consolesPanel.setActiveConsole(console);
            consolesPanel.sendTextToConsole(console, content);
            HybrisToolWindowService.Companion.getInstance(project).activateToolWindow();
            doExecute(consolesPanel);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final VirtualFile file = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        final boolean enabled = file != null && file.getName().endsWith(getExtension());
        e.getPresentation().setEnabledAndVisible(enabled);
    }

}
