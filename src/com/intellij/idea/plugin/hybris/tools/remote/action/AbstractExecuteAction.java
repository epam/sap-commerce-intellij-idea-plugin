package com.intellij.idea.plugin.hybris.tools.remote.action;

import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole;
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesPanel;
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesToolWindow;
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
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
        if (editor != null) {
            final SelectionModel selectionModel = editor.getSelectionModel();
            String content = selectionModel.getSelectedText();
            if (content == null || content.trim().isEmpty()) {
                content = editor.getDocument().getText();
            }

            final HybrisConsolesToolWindow consolePanelView = e.getProject().getService(HybrisConsolesToolWindow.class);
            final HybrisConsolesPanel consolePanel = consolePanelView.getConsolesPanel();

            final HybrisConsole console = consolePanel.findConsole(getConsoleName());
            if (console == null) {
                LOG.warn("unable to find console "+getConsoleName());
                return;
            }
            consolePanel.setActiveConsole(console);
            consolePanel.sendTextToConsole(console, content);
            doExecute(consolePanel);

            final ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow(HybrisToolWindowFactory.ID);
            toolWindow.activate(null);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final VirtualFile file = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        final boolean enabled = file != null && file.getName().endsWith(getExtension());
        e.getPresentation().setEnabledAndVisible(enabled);
    }

}
