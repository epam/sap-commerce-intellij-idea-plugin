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
import com.intellij.idea.plugin.hybris.settings.TSDiagramSettings;
import com.intellij.idea.plugin.hybris.settings.components.TSDiagramSettingsExcludedTypeNameTable;
import com.intellij.idea.plugin.hybris.settings.components.TSTypeNameHolder;
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;
import java.util.stream.Collectors;

public class HybrisTypeSystemDiagramSettingsForm implements Disposable {

    private final Project myProject;

    private JPanel myExcludedTypeNamesPane;
    private JPanel mySettingsPane;
    private JPanel mainPanel;
    private JBCheckBox myNodesCollapsedByDefault;
    private JBCheckBox myShowOOTBMapNodes;
    private JBCheckBox myShowCustomAtomicNodes;
    private JBCheckBox myShowCustomCollectionNodes;
    private JBCheckBox myShowCustomEnumNodes;
    private JBCheckBox myShowCustomMapNodes;
    private JBCheckBox myShowCustomRelationNodes;
    private AbstractTable<TSDiagramSettings, TSTypeNameHolder> myExcludedTypeNamesTable;

    public HybrisTypeSystemDiagramSettingsForm(final Project myProject) {
        this.myProject = myProject;
    }

    public HybrisTypeSystemDiagramSettingsForm init(final Project project) {
        return this;
    }

    public HybrisTypeSystemDiagramSettingsForm setData(final Project project) {
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var typeSystemDiagramSettings = developerSettingsComponent.getState().getTypeSystemDiagramSettings();

        myExcludedTypeNamesTable.updateModel(typeSystemDiagramSettings);
        myNodesCollapsedByDefault.setSelected(typeSystemDiagramSettings.getNodesCollapsedByDefault());
        myShowOOTBMapNodes.setSelected(typeSystemDiagramSettings.getShowOOTBMapNodes());
        myShowCustomAtomicNodes.setSelected(typeSystemDiagramSettings.getShowCustomAtomicNodes());
        myShowCustomCollectionNodes.setSelected(typeSystemDiagramSettings.getShowCustomCollectionNodes());
        myShowCustomEnumNodes.setSelected(typeSystemDiagramSettings.getShowCustomEnumNodes());
        myShowCustomMapNodes.setSelected(typeSystemDiagramSettings.getShowCustomMapNodes());
        myShowCustomRelationNodes.setSelected(typeSystemDiagramSettings.getShowCustomRelationNodes());

        return this;
    }

    public void apply(final Project project) {
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var typeSystemDiagramSettings = developerSettingsComponent.getState().getTypeSystemDiagramSettings();

        typeSystemDiagramSettings.setNodesCollapsedByDefault(myNodesCollapsedByDefault.isSelected());
        typeSystemDiagramSettings.setShowOOTBMapNodes(myShowOOTBMapNodes.isSelected());
        typeSystemDiagramSettings.setShowCustomAtomicNodes(myShowCustomAtomicNodes.isSelected());
        typeSystemDiagramSettings.setShowCustomCollectionNodes(myShowCustomCollectionNodes.isSelected());
        typeSystemDiagramSettings.setShowCustomEnumNodes(myShowCustomEnumNodes.isSelected());
        typeSystemDiagramSettings.setShowCustomMapNodes(myShowCustomMapNodes.isSelected());
        typeSystemDiagramSettings.setShowCustomRelationNodes(myShowCustomRelationNodes.isSelected());
        typeSystemDiagramSettings.setExcludedTypeNames(getCurrentExcludedTypeNames());
    }

    @NotNull
    private Set<String> getCurrentExcludedTypeNames() {
        return myExcludedTypeNamesTable.getItems().stream()
            .map(TSTypeNameHolder::getTypeName)
            .collect(Collectors.toSet());
    }

    public boolean isModified(final Project project) {
        final var developerSettingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project);
        final var typeSystemDiagramSettings = developerSettingsComponent.getState().getTypeSystemDiagramSettings();
        return myNodesCollapsedByDefault.isSelected() != typeSystemDiagramSettings.getNodesCollapsedByDefault()
            || myShowOOTBMapNodes.isSelected() != typeSystemDiagramSettings.getShowOOTBMapNodes()
            || myShowCustomAtomicNodes.isSelected() != typeSystemDiagramSettings.getShowOOTBMapNodes()
            || myShowCustomCollectionNodes.isSelected() != typeSystemDiagramSettings.getShowCustomAtomicNodes()
            || myShowCustomEnumNodes.isSelected() != typeSystemDiagramSettings.getShowCustomCollectionNodes()
            || myShowCustomMapNodes.isSelected() != typeSystemDiagramSettings.getShowCustomEnumNodes()
            || myShowCustomRelationNodes.isSelected() != typeSystemDiagramSettings.getShowCustomMapNodes()
            || !CollectionUtils.isEqualCollection(getCurrentExcludedTypeNames(), typeSystemDiagramSettings.getExcludedTypeNames());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        mySettingsPane = new JBPanel();

        myExcludedTypeNamesTable = TSDiagramSettingsExcludedTypeNameTable.Companion.getInstance(myProject);
        myExcludedTypeNamesPane = ToolbarDecorator.createDecorator(myExcludedTypeNamesTable)
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();

        mySettingsPane.setBorder(IdeBorderFactory.createTitledBorder("Common Settings"));
        myExcludedTypeNamesPane.setBorder(IdeBorderFactory.createTitledBorder("Excluded Type Names"));
    }

    @Override
    public void dispose() {

    }
}
