/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.ant;

import com.intellij.ide.DataManager;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.actions.ProjectRefreshAction;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.lang.ant.config.AntBuildFileBase;
import com.intellij.lang.ant.config.AntBuildListener;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.execution.AntBeforeExecutionEvent;
import com.intellij.lang.ant.config.execution.AntExecutionListener;
import com.intellij.lang.ant.config.execution.AntFinishedExecutionEvent;
import com.intellij.lang.ant.config.execution.ExecutionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Martin Zdarsky-Jones on 15/2/17.
 * <p>
 * This implementation of AntBuildListener gets exchanged for the blank anonymous implementation of AntBuildListener.NULL
 * That way intellij ant plugin will pick up our listener and we'll kow when ant build finishes.
 * However, we will not know what project finished. We'll just know that some ant build of some project finished.
 * <p>
 * The ant logger will leave serialized class describing new/old extensions if there are any.
 * We need to search through all open projects to see if there is a "message" from ant process.
 * If there is then we also find out which project left the message.
 */
public class HybrisAntBuildListener implements AntExecutionListener {

    private static final Key<STATES> STATE = Key.create("hybrisAntStateMachine");
    private static final List<String> antCleanAll = Arrays.asList("clean", "all");

    public static void registerAntListener(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(AntExecutionListener.TOPIC, new HybrisAntBuildListener());
    }

    @Override
    public void beforeExecution(final AntBeforeExecutionEvent event) {
    }

    @Override
    public void buildFinished(final AntFinishedExecutionEvent event) {
        final Map<Project, AntGenResult> resultMap = new HashMap<>();
        findAntResult(resultMap);
        processNewExtensions(resultMap);
        triggerNextAction();
    }

    private void findAntResult(final Map<Project, AntGenResult> resultMap) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            final var settings = HybrisProjectSettingsComponent.getInstance(project);
            final var hybrisProjectSettings = settings.getState();

            if (!settings.isHybrisProject()) {
                continue;
            }

            final File file = new File(project.getBasePath() + "/" + hybrisProjectSettings.getHybrisDirectory() + "/temp/ant.ser");
            if (file.exists()) {
                AntGenResult result = null;
                try (
                    final FileInputStream fileIn = new FileInputStream(file);
                    final ObjectInputStream in = new ObjectInputStream(fileIn)
                ) {
                    result = (AntGenResult) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                FileUtil.delete(file);
                resultMap.put(project, result);
                return;
            }
        }
    }

    private void processNewExtensions(final Map<Project, AntGenResult> resultMap) {
        if (!resultMap.isEmpty()) {
            final Project project = resultMap.keySet().iterator().next();
            final AntGenResult antGenResult = resultMap.get(project);
            modifyLocalExtensions(project, antGenResult);
            project.putUserData(STATE, STATES.CLEAN_ALL_NEEDED);
        }
    }

    private void triggerNextAction() {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            final STATES state = project.getUserData(STATE);
            if (state == null) {
                continue;
            }
            switch (state) {
                case CLEAN_ALL_NEEDED:
                    project.putUserData(STATE, STATES.REFRESH_NEEDED);
                    triggerCleanAll(project);
                    return;
                case REFRESH_NEEDED:
                    project.putUserData(STATE, null);
                    ProjectRefreshAction.triggerAction(getDataContext(project));
                    return;
            }
        }
    }

    private void modifyLocalExtensions(final Project project, final AntGenResult result) {
        final HybrisProjectSettings hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                                          .getState();
        final File file = new File(project.getBasePath() + "/" +
                                   hybrisProjectSettings.getHybrisDirectory() + "/config/" + HybrisConstants.LOCAL_EXTENSIONS_XML);
        final VirtualFile vf = VfsUtil.findFileByIoFile(file, true);

        if (vf == null) {
            return;
        }
        final XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(vf);

        if (xmlFile == null) {
            return;
        }
        WriteCommandAction.writeCommandAction(project, xmlFile).run(() -> {

            final XmlTag hybrisconfig = xmlFile.getRootTag();
            if (hybrisconfig == null) {
                return;
            }
            for (XmlTag extensions : hybrisconfig.getSubTags()) {
                if (!extensions.getName().equals("extensions")) {
                    continue;
                }
                for (XmlTag extension : extensions.getSubTags()) {
                    if (!extension.getName().equals("extension")) {
                        continue;
                    }
                    if (result.getExtensionsToRemove().contains(extension.getAttributeValue("name"))) {
                        extension.delete();
                    }
                }

                for (String newExtension : result.getExtensionsToAdd()) {
                    final XmlTag newTag = extensions.createChildTag("extension", null, null, false);
                    final String name = newExtension.substring(newExtension.lastIndexOf("/") + 1);
                    final String dir = "${HYBRIS_BIN_DIR}" + newExtension.substring(newExtension.indexOf("/custom"));
                    newTag.setAttribute("dir", dir);
                    newTag.setAttribute("name", name);
                    extensions.addSubTag(newTag, false);
                }
            }

            FileDocumentManager.getInstance().saveAllDocuments();
        });
    }

    private void triggerCleanAll(final Project project) {
        final HybrisProjectSettings yProjectSettings = HybrisProjectSettingsComponent.getInstance(project).getState();
        final File platformDir = new File(project.getBasePath() + "/" +
                                          yProjectSettings.getHybrisDirectory() + HybrisConstants.PLATFORM_MODULE_PREFIX);
        final VirtualFile vfPlatformDir = VfsUtil.findFileByIoFile(platformDir, true);
        final VirtualFile vfBuildFile = VfsUtil.findRelativeFile(vfPlatformDir, HybrisConstants.ANT_BUILD_XML);

        if (vfBuildFile == null) {
            return;
        }
        final PsiFile psiBuildFile = PsiManager.getInstance(project).findFile(vfBuildFile);

        if (psiBuildFile == null) {
            return;
        }
        final AntConfigurationBase antConfiguration = AntConfigurationBase.getInstance(project);
        final AntBuildFileBase antBuildFile = antConfiguration.getAntBuildFile(psiBuildFile);

        if (antBuildFile != null) {
            ExecutionHandler.runBuild(
                antBuildFile,
                antCleanAll,
                null,
                getDataContext(project),
                Collections.emptyList(),
                AntBuildListener.NULL
            );
        }
    }

    private DataContext getDataContext(final Project project) {
        final WindowManagerEx windowManager = WindowManagerEx.getInstanceEx();
        final Component focusedComponent = windowManager.getFocusedComponent(project);
        return DataManager.getInstance().getDataContext(focusedComponent);
    }

    public enum STATES {CLEAN_ALL_NEEDED, REFRESH_NEEDED}
}
