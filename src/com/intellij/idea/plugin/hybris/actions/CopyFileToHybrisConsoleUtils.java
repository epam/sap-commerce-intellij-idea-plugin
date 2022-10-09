/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.actions;

import com.intellij.execution.console.ConsoleExecutionEditor;
import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole;
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory;
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesPanel;
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesToolWindow;
import com.intellij.idea.plugin.hybris.toolwindow.CopyFileToHybrisConsoleDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.DIALOG_TITLE;
import static java.lang.System.lineSeparator;

public final class CopyFileToHybrisConsoleUtils {

    private CopyFileToHybrisConsoleUtils() {
    }

    private static <R, T> Optional<T> cast(@NotNull R obj, Class<T> clazz) {
        if (clazz.isAssignableFrom(obj.getClass())) {
            return Optional.ofNullable((T) obj);
        } else {
            return Optional.empty();
        }
    }

    public static void copySelectedFilesToHybrisConsole(Project project, String consoleTitle, String dialogTitle) {
        var hybrisConsole = getHybrisConsole(project, consoleTitle);
        if (hybrisConsole != null) {
            boolean isConsoleNotEmpty = StringUtils.isNotEmpty(getTextFromHybrisConsole(project, hybrisConsole));
            String query = getQueryFromSelectedFiles(project);
            if (isConsoleNotEmpty) {
                var consoleDialog = new CopyFileToHybrisConsoleDialog(
                    project,
                    getDialogTitleFromProperties(dialogTitle)
                );
                consoleDialog.show(() -> copyToHybrisConsole(project, consoleTitle, query));
            } else {
                copyToHybrisConsole(project, consoleTitle, query);
            }
        }
    }

    public static boolean isRequiredSingleFileExtension(Project project, String fileExtension) {
        var fileExtensions = getFileExtensions(project);
        return fileExtensions.size() == 1 && fileExtensions.get(0).equals(fileExtension);
    }

    public static boolean isRequiredMultipleFileExtension(Project project, String fileExtension) {
        var fileExtensions = getFileExtensions(project);
        return !fileExtensions.isEmpty() && fileExtensions.stream().allMatch(fileExtension::equals);
    }

    private static List<String> getFileExtensions(Project project) {
        var extensions = new ArrayList<String>();
        for (var virtualFile : getSelectedFiles(project)) {
            if (virtualFile.isDirectory()) {
                return Collections.emptyList();
            }
            extensions.add(virtualFile.getExtension());
        }
        return extensions;
    }

    private static HybrisConsole getHybrisConsole(Project project, String consoleTitle) {
        var hybrisConsolePanel = HybrisConsolesToolWindow.Companion.getInstance(project).getConsolesPanel();
        return hybrisConsolePanel.findConsole(consoleTitle);
    }

    private static String getQueryFromSelectedFiles(Project project) {
        return getSelectedFiles(project).stream()
                                        .map(virtualFile -> getPsiFileNode(project, virtualFile))
                                        .map(PsiElement::getText)
                                        .collect(Collectors.joining(lineSeparator()));
    }

    private static PsiFile getPsiFileNode(Project project, VirtualFile virtualFile) {
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    private static List<VirtualFile> getSelectedFiles(Project project) {
        return Arrays.stream(getSelectedTreePaths(project))
                     .map(CopyFileToHybrisConsoleUtils::getVirtualFile)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());
    }

    @NotNull
    private static Optional<VirtualFile> getVirtualFile(final TreePath treePath) {
        return cast(treePath.getLastPathComponent(), DefaultMutableTreeNode.class)
            .flatMap(lastPathNode -> cast(lastPathNode.getUserObject(), ProjectViewNode.class))
            .map(ProjectViewNode::getVirtualFile);
    }

    private static TreePath[] getSelectedTreePaths(final Project project) {
        var currentProjectViewPane = ProjectView.getInstance(project).getCurrentProjectViewPane();
        var selectionPaths = currentProjectViewPane.getSelectionPaths();
        return ObjectUtils.getIfNull(selectionPaths, () -> new TreePath[0]);
    }

    private static HybrisConsolesPanel getHybrisConsolePanel(Project project) {
        return HybrisConsolesToolWindow.Companion.getInstance(project).getConsolesPanel();
    }

    private static String getTextFromHybrisConsole(Project project, HybrisConsole hybrisConsole) {
        var helper = new LanguageConsoleImpl.Helper(project, hybrisConsole.getVirtualFile());
        var consoleExecutionEditor = new ConsoleExecutionEditor(helper);
        return consoleExecutionEditor.getDocument().getText();
    }

    private static void copyToHybrisConsole(Project project, String consoleTitle, String query) {
        var hybrisConsolePanel = getHybrisConsolePanel(project);
        var hybrisConsole = hybrisConsolePanel.findConsole(consoleTitle);
        if (hybrisConsole != null) {
            hybrisConsole.clear();
            hybrisConsole.setInputText(query);
            hybrisConsolePanel.setActiveConsole(hybrisConsole);
            openHybrisConsole(project);
        }
    }

    private static void openHybrisConsole(Project project) {
        var toolWindow = ToolWindowManager.getInstance(project).getToolWindow(HybrisToolWindowFactory.ID);
        if (toolWindow != null) {
            toolWindow.activate(null);
        }
    }

    private static String getDialogTitleFromProperties(String fileExtension) {
        return HybrisI18NBundleUtils.message(DIALOG_TITLE + fileExtension);
    }
}