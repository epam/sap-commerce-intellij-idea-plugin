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

package com.intellij.idea.plugin.hybris.settings.forms;

import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class HybrisProjectRemoteInstancesSettingsForm {

    private Project myProject;
    private JPanel mainPanel;
    private JPanel hacConnectionSettings;
    private JPanel solrConnectionSettings;
    private RemoteInstancesListPanel hacInstances;
    private RemoteInstancesListPanel solrInstances;

    public HybrisProjectRemoteInstancesSettingsForm(@NotNull final Project project) {
        myProject = project;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setData() {
        final var component = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject);

        hacInstances.setInitialList(component.getHacRemoteConnectionSettings());
        solrInstances.setInitialList(component.getSolrRemoteConnectionSettings());
    }

    private void createUIComponents() {
        hacInstances = new RemoteHacInstancesListPanel(myProject, message("hybris.settings.project.remote_instances.hac.title"), new ArrayList<>());
        hacConnectionSettings = hacInstances;
        solrInstances = new RemoteSolrInstancesListPanel(myProject, message("hybris.settings.project.remote_instances.solr.title"), new ArrayList<>());
        solrConnectionSettings = solrInstances;
    }
}
