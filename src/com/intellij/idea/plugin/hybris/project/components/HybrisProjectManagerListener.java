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

package com.intellij.idea.plugin.hybris.project.components;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.idea.plugin.hybris.ant.HybrisAntBuildListener;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.services.NotificationService;
import com.intellij.idea.plugin.hybris.common.services.impl.DefaultNotificationService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.project.actions.ProjectRefreshAction;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.ConsolePersistenceService;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.openapi.updateSettings.impl.UpdateInstaller;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.spring.settings.SpringGeneralSettings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static com.intellij.idea.plugin.hybris.project.utils.PluginCommon.ANT_SUPPORT_PLUGIN_ID;
import static com.intellij.idea.plugin.hybris.project.utils.PluginCommon.SPRING_PLUGIN_ID;
import static com.intellij.idea.plugin.hybris.project.utils.PluginCommon.isPluginActive;
import static com.intellij.openapi.util.io.FileUtilRt.toSystemDependentName;

/**
 * Created by Martin Zdarsky-Jones on 29/09/2016.
 */
public class HybrisProjectManagerListener implements ProjectManagerListener, Disposable {

    private static final Logger LOG = Logger.getInstance(HybrisProjectManagerListener.class);

    private static final NotificationGroup Y_PROJECT_NOTIFICATION_GROUP = new NotificationGroup(
        "[y] project",
        NotificationDisplayType.BALLOON,
        true,
        null,
        HybrisIcons.HYBRIS_ICON
    );

    @Override
    public void projectOpened(@NotNull final Project project) {
        StartupManager.getInstance(project).registerPostStartupActivity((DumbAwareRunnable) () -> {
            if (project.isDisposed()) {
                return;
            }

            final NotificationService notificationService = new DefaultNotificationService(
                Y_PROJECT_NOTIFICATION_GROUP, project
            );

            if (isOldHybrisProject(project)) {
                notificationService.showImportantNotification(
                    "hybris.project.open.outdated.title",
                    "hybris.project.open.outdated.text",
                    NotificationType.INFORMATION,
                    (myNotification, myHyperlinkEvent) -> ProjectRefreshAction.triggerAction()
                );
            }

            final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
            if (!commonIdeaService.isHybrisProject(project)) {
                return;
            }

            logVersion(project);

            continueOpening(project);
        });
    }

    private void logVersion(final Project project) {
        final HybrisProjectSettings settings = HybrisProjectSettingsComponent.getInstance(project).getState();
        final String importedBy = settings.getImportedByVersion();
        final String hybrisVersion = settings.getHybrisVersion();
        final String pluginVersion = PluginManager.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID)).getVersion();
        LOG.info("Opening hybris version " + hybrisVersion + " which was imported by " + importedBy + ". Current plugin is " + pluginVersion);
    }

    private void continueOpening(final Project project) {
        if (project.isDisposed()) {
            return;
        }
        registerAntListener(project);
        resetSpringGeneralSettings(project);
        fixBackOfficeJRebelSupport(project);
        CommonIdeaService.getInstance().fixRemoteConnectionSettings(project);
        ConsolePersistenceService.getInstance(project).loadPersistedQueries();
        checkForUpdates();
    }

    private void checkForUpdates() {
        UpdateSettings.getInstance().setCheckNeeded(true);
        UpdateSettings.getInstance().forceCheckForUpdateAfterRestart();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Collection<PluginDownloader> availableUpdates = UpdateChecker.getPluginUpdates();
            if (availableUpdates == null) {
                LOG.info("Some plugin updates found");
                return;
            }
            PluginDownloader pluginDownloader =
                availableUpdates.stream()
                                .filter(downloader -> HybrisConstants.PLUGIN_ID.equals(downloader.getId().getIdString()))
                                .findAny().orElse(null);
            if (pluginDownloader == null) {
                LOG.info("SAP Commerce Developers Toolset plugin update not found");
                return;
            }
            LOG.info("SAP Commerce Developers Toolset plugin update available");
            if (UpdateInstaller.installPluginUpdates(availableUpdates, new EmptyProgressIndicator())) {
                LOG.info("SAP Commerce Developers Toolset plugin update succeeded");
            } else {
                LOG.info("SAP Commerce Developers Toolset plugin update failed");
            }
        });
    }

    private void resetSpringGeneralSettings(final Project project) {
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        if (commonIdeaService.isHybrisProject(project)) {
            if (isPluginActive(SPRING_PLUGIN_ID)) {
                SpringGeneralSettings springGeneralSettings = SpringGeneralSettings.getInstance(project);
                springGeneralSettings.setShowMultipleContextsPanel(false);
                springGeneralSettings.setShowProfilesPanel(false);
            }
        }
    }

    private void registerAntListener(final Project project) {
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        if (commonIdeaService.isHybrisProject(project)) {
            if (isPluginActive(ANT_SUPPORT_PLUGIN_ID)) {
                HybrisAntBuildListener.registerAntListener(project);
            }
        }
    }

    private boolean isOldHybrisProject(final Project project) {
        final CommonIdeaService commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService.class);
        if (commonIdeaService.isHybrisProject(project)) {
            return commonIdeaService.isOutDatedHybrisProject(project);
        } else {
            return commonIdeaService.isPotentiallyHybrisProject(project);
        }
    }

    private void fixBackOfficeJRebelSupport(final Project project) {
        Validate.notNull(project);

        final IdeaPluginDescriptor jRebelPlugin = PluginManager.getPlugin(PluginId.getId(HybrisConstants.JREBEL_PLUGIN_ID));
        if (jRebelPlugin == null || !jRebelPlugin.isEnabled()) {
            return;
        }
        final HybrisProjectSettings hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                                          .getState();
        final File compilingXml = new File(toSystemDependentName(project.getBasePath() + "/" + hybrisProjectSettings.getHybrisDirectory() +
                                                                 HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.ANT_COMPILING_XML));
        if (!compilingXml.isFile()) {
            return;
        }
        String content;
        try {
            content = IOUtils.toString(new FileInputStream(compilingXml), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error(e);
            return;
        }
        if (!content.contains("excludes=\"**/rebel.xml\"")) {
            return;
        }
        content = content.replace("excludes=\"**/rebel.xml\"", "");
        try {
            IOUtils.write(content, new FileOutputStream(compilingXml), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error(e);
            return;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void projectClosing(@NotNull final Project project) {
        ConsolePersistenceService.getInstance(project).persistQueryRegions();
    }


}