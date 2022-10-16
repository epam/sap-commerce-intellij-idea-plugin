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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.forms;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.model.Cardinality;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;

public class TSMetaRelationView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTabbedPane myTabs;
    private ComboBox<Cardinality> myCardinalitySource;
    private ComboBox<Cardinality> myCardinalityTarget;
    private JBTextField myTypecode;
    private JBTextField myDeploymentTable;
    private JBTextField myCode;
    private JTextPane myDescription;
    private JBCheckBox myLocalized;
    private JBCheckBox myAutocreate;
    private JBCheckBox myGenerate;
    private JBTextField mySourceType;
    private JBTextField myTargetType;
    private final TSMetaRelationElementView mySourceView;
    private final TSMetaRelationElementView myTargetView;

    public TSMetaRelationView(final Project project) {
        myProject = project;
        mySourceView = new TSMetaRelationElementView(myProject);
        myTargetView = new TSMetaRelationElementView(myProject);

        myTabs.insertTab("Source", null, mySourceView.getContent(), null, 1);
        myTabs.insertTab("Target", null, myTargetView.getContent(), null, 2);
    }

    private void initData(final TSMetaRelation myMeta) {
        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myAutocreate.setSelected(myMeta.isAutocreate());
        myLocalized.setSelected(myMeta.isLocalized());
        myGenerate.setSelected(myMeta.isGenerate());

        mySourceView.updateView(myMeta.getSource());
        myTargetView.updateView(myMeta.getTarget());
        myCardinalitySource.setSelectedItem(myMeta.getSource().getCardinality());
        myCardinalityTarget.setSelectedItem(myMeta.getTarget().getCardinality());
        mySourceType.setText(myMeta.getSource().getType());
        myTargetType.setText(myMeta.getTarget().getType());

        myDeploymentTable.setText(null);
        myTypecode.setText(null);
        Optional.ofNullable(myMeta.getDeployment())
            .ifPresent(deployment -> {
                myDeploymentTable.setText(deployment.getTable());
                myTypecode.setText(deployment.getTypeCode());
            });
    }

    public JBPanel getContent(final TSMetaRelation meta) {
        initData(meta);

        return myContentPane;
    }

    private void createUIComponents() {
        myCardinalitySource = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Cardinality.values())));
        myCardinalityTarget = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Cardinality.values())));
    }
}