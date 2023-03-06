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
import com.intellij.idea.plugin.hybris.toolwindow.CopyFileToHybrisConsoleDialog;
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory;
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.DIALOG_TITLE;
import static java.lang.System.lineSeparator;

public final class CopyFileToHybrisConsoleUtils {

    private CopyFileToHybrisConsoleUtils() {
    }

    private static <R, T> Optional<T> cast(@NotNull final R obj, final Class<T> clazz) {
        if (clazz.isAssignableFrom(obj.getClass())) {
            return Optional.ofNullable((T) obj);
        } else {
            return Optional.empty();
        }
    }

    public static void copySelectedFilesToHybrisConsole(final Project project, final String consoleTitle, final String dialogTitle) {
        final var hybrisConsole = HybrisToolWindowService.Companion.getInstance(project).getConsolesPanel().findConsole(consoleTitle);
        if (hybrisConsole != null) {
            final boolean isConsoleNotEmpty = StringUtils.isNotEmpty(getTextFromHybrisConsole(project, hybrisConsole));
            final String query = getQueryFromSelectedFiles(project);
            if (isConsoleNotEmpty) {
                final var consoleDialog = new CopyFileToHybrisConsoleDialog(
                    project,
                    getDialogTitleFromProperties(dialogTitle)
                );
                consoleDialog.show(() -> copyToHybrisConsole(project, consoleTitle, query));
            } else {
                copyToHybrisConsole(project, consoleTitle, query);
            }
        }
    }

    public static boolean isRequiredSingleFileExtension(final Project project, final String fileExtension) {
        final var fileExtensions = getFileExtensions(project);
        return fileExtensions.size() == 1 && fileExtensions.get(0).equals(fileExtension);
    }

    public static boolean isRequiredMultipleFileExtension(final Project project, final String fileExtension) {
        final var fileExtensions = getFileExtensions(project);
        return !fileExtensions.isEmpty() && fileExtensions.stream().allMatch(fileExtension::equals);
    }

    private static List<String> getFileExtensions(final Project project) {
        final var extensions = new ArrayList<String>();
        for (var virtualFile : getSelectedFiles(project)) {
            if (virtualFile.isDirectory()) {
                return Collections.emptyList();
            }
            if (virtualFile.getExtension() != null) {
                extensions.add(virtualFile.getExtension());
            }
        }
        return extensions;
    }

    private static String getQueryFromSelectedFiles(final Project project) {
        return getSelectedFiles(project).stream()
                                        .map(virtualFile -> getPsiFileNode(project, virtualFile))
                                        .map(PsiElement::getText)
                                        .collect(Collectors.joining(lineSeparator()));
    }

    private static PsiFile getPsiFileNode(final Project project, final VirtualFile virtualFile) {
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    private static List<VirtualFile> getSelectedFiles(final Project project) {
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
        final var currentProjectViewPane = ProjectView.getInstance(project).getCurrentProjectViewPane();
        final var selectionPaths = currentProjectViewPane.getSelectionPaths();
        return ObjectUtils.getIfNull(selectionPaths, () -> new TreePath[0]);
    }

    private static String getTextFromHybrisConsole(final Project project, final HybrisConsole hybrisConsole) {
        final var helper = new LanguageConsoleImpl.Helper(project, hybrisConsole.getVirtualFile());
        final var consoleExecutionEditor = new ConsoleExecutionEditor(helper);
        final var text = consoleExecutionEditor.getDocument().getText();
        Disposer.dispose(consoleExecutionEditor);
        return text;
    }

    private static void copyToHybrisConsole(final Project project, final String consoleTitle, final String query) {
        final var consolesPanel = HybrisToolWindowService.Companion.getInstance(project).getConsolesPanel();
        final var hybrisConsole = consolesPanel.findConsole(consoleTitle);
        if (hybrisConsole != null) {
            final var toolWindowService = HybrisToolWindowService.Companion.getInstance(project);
            toolWindowService.activateToolWindow();
            toolWindowService.activateToolWindowTab(HybrisToolWindowFactory.CONSOLES_ID);
            consolesPanel.setActiveConsole(hybrisConsole);
            hybrisConsole.clear();
            hybrisConsole.setInputText(query);
        }
    }

    private static String getDialogTitleFromProperties(final String fileExtension) {
        return HybrisI18NBundleUtils.message(DIALOG_TITLE + fileExtension);
    }
}