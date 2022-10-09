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

package com.intellij.idea.plugin.hybris.type.system.validation.impl;

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.services.NotificationService;
import com.intellij.idea.plugin.hybris.common.services.impl.DefaultNotificationService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisXmlFileType;
import com.intellij.idea.plugin.hybris.type.system.validation.ItemsFileValidation;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TS_ITEMS_VALIDATION_WARN;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ProjectOpenAndItemsXmlFileOpenListener implements ProjectManagerListener {

    private static final String ITEM_XML_VALIDATION_GROUP = "Items XML validation group";

    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(
        ITEM_XML_VALIDATION_GROUP, NotificationDisplayType.BALLOON, true
    );

    @Override
    public void projectOpened(@Nonnull final Project project) {
        if (!ApplicationManager.getApplication().getService(CommonIdeaService.class).isHybrisProject(project)) {
            return;
        }
        final NotificationService notificationService = new DefaultNotificationService(NOTIFICATION_GROUP, project);

        project.getMessageBus().connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER, new ItemsXmlFileEditorManagerListener(project, notificationService)
        );

        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            final Collection<VirtualFile> files = FileTypeIndex.getFiles(
                HybrisXmlFileType.INSTANCE, GlobalSearchScope.projectScope(project)
            );

            if (files.stream().anyMatch(new DefaultItemsFileValidation(project)::isFileOutOfDate)) {
                notificationService.showNotificationWithCloseTimeout(
                    TS_ITEMS_VALIDATION_WARN, NotificationType.WARNING, 5000
                );
            }
        });
    }

    private static class ItemsXmlFileEditorManagerListener implements FileEditorManagerListener {

        private final Project project;
        private final NotificationService notifications;
        private ItemsFileValidation validator;

        public ItemsXmlFileEditorManagerListener(
            @NotNull final Project project,
            @NotNull final NotificationService notifications
        ) {
            this.project = project;
            this.notifications = notifications;
            validator = new DefaultItemsFileValidation(project);
        }

        @Override
        public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
            StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
                if (this.validator.isFileOutOfDate(file)) {
                    notifications.showNotification(TS_ITEMS_VALIDATION_WARN, NotificationType.WARNING);
                }
            });
        }
    }
}
