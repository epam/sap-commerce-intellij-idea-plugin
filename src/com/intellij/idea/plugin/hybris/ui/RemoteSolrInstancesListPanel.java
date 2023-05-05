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

package com.intellij.idea.plugin.hybris.ui;

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings.Type;
import com.intellij.idea.plugin.hybris.toolwindow.RemoteSolrConnectionDialog;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.Serial;

public class RemoteSolrInstancesListPanel extends RemoteInstancesListPanel {

    @Serial
    private static final long serialVersionUID = -6666004870055817895L;

    public RemoteSolrInstancesListPanel(final Project project) {
        super(project);
    }

    @Override
    protected @Nullable HybrisRemoteConnectionSettings editSelectedItem(final HybrisRemoteConnectionSettings item) {
        final boolean ok = new RemoteSolrConnectionDialog(myProject, this, item).showAndGet();
        return ok ? item : null;
    }

    @Override
    Icon getCellIcon() {
        return HybrisIcons.CONSOLE_SOLR;
    }

    @Override
    void saveSettings() {
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).saveRemoteConnectionSettingsList(Type.SOLR, getData());
    }

    @Override
    void addItem() {
        final var item = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).getDefaultSolrRemoteConnectionSettings(myProject);
        final var dialog = new RemoteSolrConnectionDialog(myProject, this, item);
        if (dialog.showAndGet()) {
            addElement(item);
        }
    }
}
