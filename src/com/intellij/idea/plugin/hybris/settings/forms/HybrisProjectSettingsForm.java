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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsListener;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class HybrisProjectSettingsForm implements Disposable {

    private JPanel mainPanel;
    private JCheckBox createBackwardCyclicDependenciesForAddons;
    private JCheckBox importOotbModulesInReadOnlyMode;
    private JCheckBox followSymlink;
    private JCheckBox scanThroughExternalModule;
    private JCheckBox excludeTestSources;
    private JTextField hybrisVersion;
    private JTextField javadocUrl;
    private JTextField hybrisDirectory;
    private JComboBox activeHacInstance;
    private JComboBox activeSolrInstance;
    private JPanel panelProjectDetails;
    private JPanel panelProjectRefresh;
    private JPanel panelRemoteInstances;
    private JLabel labelActiveSolrInstance;
    private JLabel labelActiveHacInstance;
    private DefaultComboBoxModel<HybrisRemoteConnectionSettings> activeHacServerModel;
    private DefaultComboBoxModel<HybrisRemoteConnectionSettings> activeSolrServerModel;

    public HybrisProjectSettingsForm init(final Project project) {
        project.getMessageBus().connect(this).subscribe(HybrisDeveloperSpecificProjectSettingsListener.TOPIC,
            new HybrisDeveloperSpecificProjectSettingsListener(){
                @Override
                public void hacConnectionSettingsChanged() {
                    final var settingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
                    updateRemoteInstancesModel(
                        activeHacServerModel,
                        settingsComponent.getActiveHacRemoteConnectionSettings(project),
                        settingsComponent.getHacRemoteConnectionSettings()
                    );
                }

                @Override
                public void solrConnectionSettingsChanged() {
                    final var settingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
                    updateRemoteInstancesModel(
                        activeSolrServerModel,
                        settingsComponent.getActiveSolrRemoteConnectionSettings(project),
                        settingsComponent.getSolrRemoteConnectionSettings()
                    );
                }
            }
        );
        return this;
    }

    public HybrisProjectSettingsForm setData(final Project project) {
        final var projectSettingsComponent = HybrisProjectSettingsComponent.getInstance(project);
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var projectSettings = projectSettingsComponent.getState();

        updateRemoteInstancesModel(
            activeHacServerModel,
            developerSettingsComponent.getActiveHacRemoteConnectionSettings(project),
            developerSettingsComponent.getHacRemoteConnectionSettings()
        );
        updateRemoteInstancesModel(
            activeSolrServerModel,
            developerSettingsComponent.getActiveSolrRemoteConnectionSettings(project),
            developerSettingsComponent.getSolrRemoteConnectionSettings()
        );

        createBackwardCyclicDependenciesForAddons.setSelected(projectSettings.isCreateBackwardCyclicDependenciesForAddOns());
        followSymlink.setSelected(projectSettings.isFollowSymlink());
        scanThroughExternalModule.setSelected(projectSettings.isScanThroughExternalModule());
        excludeTestSources.setSelected(projectSettings.isExcludeTestSources());
        importOotbModulesInReadOnlyMode.setSelected(projectSettings.isImportOotbModulesInReadOnlyMode());
        hybrisVersion.setText(projectSettings.getHybrisVersion());
        javadocUrl.setText(projectSettings.getJavadocUrl());
        hybrisDirectory.setText(projectSettings.getHybrisDirectory());

        return this;
    }

    public void apply(final Project project) {
        final var projectSettingsComponent = HybrisProjectSettingsComponent.getInstance(project);
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var projectSettings = projectSettingsComponent.getState();
        projectSettings.setCreateBackwardCyclicDependenciesForAddOns(createBackwardCyclicDependenciesForAddons.isSelected());
        projectSettings.setFollowSymlink(followSymlink.isSelected());
        projectSettings.setScanThroughExternalModule(scanThroughExternalModule.isSelected());
        projectSettings.setExcludeTestSources(excludeTestSources.isSelected());
        projectSettings.setImportOotbModulesInReadOnlyMode(importOotbModulesInReadOnlyMode.isSelected());
        projectSettings.setHybrisVersion(hybrisVersion.getText());
        projectSettings.setJavadocUrl(javadocUrl.getText());
        projectSettings.setHybrisDirectory(hybrisDirectory.getText());

        Optional.ofNullable(developerSettingsComponent.getState())
                .ifPresent(developerSettings -> {
                    developerSettingsComponent.setActiveHacRemoteConnectionSettings((HybrisRemoteConnectionSettings) activeHacInstance.getSelectedItem());
                    developerSettingsComponent.setActiveSolrRemoteConnectionSettings((HybrisRemoteConnectionSettings) activeSolrInstance.getSelectedItem());
                });
    }

    public boolean isModified(final Project project) {
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var projectSettings = HybrisProjectSettingsComponent.getInstance(project).getState();
        final var developerSettings = developerSettingsComponent.getState();
        if (developerSettings == null) {
            return false;
        }

        return createBackwardCyclicDependenciesForAddons.isSelected() != projectSettings.isCreateBackwardCyclicDependenciesForAddOns()
               || importOotbModulesInReadOnlyMode.isSelected() != projectSettings.isImportOotbModulesInReadOnlyMode()
               || scanThroughExternalModule.isSelected() != projectSettings.isScanThroughExternalModule()
               || followSymlink.isSelected() != projectSettings.isFollowSymlink()
               || !StringUtil.equals(hybrisVersion.getText(), projectSettings.getHybrisVersion())
               || !StringUtil.equals(javadocUrl.getText(), projectSettings.getJavadocUrl())
               || !StringUtil.equals(hybrisDirectory.getText(), projectSettings.getHybrisDirectory())
               || !Objects.equals(activeHacInstance.getSelectedItem(), developerSettingsComponent.getActiveHacRemoteConnectionSettings(project))
               || !Objects.equals(activeSolrInstance.getSelectedItem(), developerSettingsComponent.getActiveSolrRemoteConnectionSettings(project));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void updateRemoteInstancesModel(
        final DefaultComboBoxModel<HybrisRemoteConnectionSettings> model,
        final HybrisRemoteConnectionSettings activeRemoteConnection,
        final List<HybrisRemoteConnectionSettings> remoteConnectionSettingsList
    ) {
        model.removeAllElements();
        remoteConnectionSettingsList.forEach(model::addElement);
        model.setSelectedItem(activeRemoteConnection);
    }

    private void createUIComponents() {
        labelActiveHacInstance = new JBLabel();
        labelActiveSolrInstance = new JBLabel();
        labelActiveHacInstance.setIcon(HybrisIcons.HYBRIS);
        labelActiveSolrInstance.setIcon(HybrisIcons.CONSOLE_SOLR);

        panelProjectDetails = initPanel("hybris.settings.project.details.title");
        panelProjectRefresh = initPanel("hybris.settings.project.refresh.title");
        panelRemoteInstances = initPanel("hybris.settings.project.remote_instances.title");

        activeHacServerModel = new DefaultComboBoxModel<>();
        activeSolrServerModel = new DefaultComboBoxModel<>();
        activeHacInstance = new ComboBox<>(activeHacServerModel);
        activeSolrInstance = new ComboBox<>(activeSolrServerModel);
    }

    private JPanel initPanel(final String key) {
        final var panel = new JBPanel<>();
        panel.setBorder(IdeBorderFactory.createTitledBorder(message(key), true));
        return panel;
    }

    @Override
    public void dispose() {

    }
}
