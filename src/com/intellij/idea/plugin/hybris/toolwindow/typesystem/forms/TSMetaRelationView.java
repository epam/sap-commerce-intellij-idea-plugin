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
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import icons.DvcsImplIcons;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;

public class TSMetaRelationView {

    private static final int TAB_DETAILS_INDEX = 0;
    private static final int TAB_SOURCE_INDEX = 1;
    private static final int TAB_TARGET_INDEX = 2;
    private final Project myProject;

    private JBPanel myContentPane;
    private JBTabbedPane myTabs;
    private ComboBox<Cardinality> myCardinalitySource;
    private ComboBox<Cardinality> myCardinalityTarget;
    private JBTextField myTypeCode;
    private JBTextField myDeploymentTable;
    private JBTextField myCode;
    private JTextPane myDescription;
    private JBCheckBox myLocalized;
    private JBCheckBox myAutocreate;
    private JBCheckBox myGenerate;
    private JBTextField mySourceType;
    private JBTextField myTargetType;
    private JPanel myDeploymentPane;
    private JPanel myFlagsPane;
    private JBPanel myDetailsPane;
    private final TSMetaRelationElementView mySourceView;
    private final TSMetaRelationElementView myTargetView;

    public TSMetaRelationView(final Project project) {
        myProject = project;
        mySourceView = new TSMetaRelationElementView(myProject);
        myTargetView = new TSMetaRelationElementView(myProject);

        myTabs.insertTab("Source", DvcsImplIcons.Outgoing, mySourceView.getContent(), null, TAB_SOURCE_INDEX);
        myTabs.insertTab("Target", DvcsImplIcons.Incoming, myTargetView.getContent(), null, TAB_TARGET_INDEX);
    }

    private void initData(final TSMetaRelation myMeta) {
        if (StringUtils.equals(myMeta.getName(), myCode.getText())) {
            // same object, no need in re-init
            return;
        }

        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myAutocreate.setSelected(myMeta.isAutoCreate());
        myLocalized.setSelected(myMeta.isLocalized());
        myGenerate.setSelected(myMeta.isGenerate());

        mySourceView.updateView(myMeta.getSource());
        myTargetView.updateView(myMeta.getTarget());
        myCardinalitySource.setSelectedItem(myMeta.getSource().getCardinality());
        myCardinalityTarget.setSelectedItem(myMeta.getTarget().getCardinality());
        mySourceType.setText(myMeta.getSource().getType());
        myTargetType.setText(myMeta.getTarget().getType());

        myDeploymentTable.setText(null);
        myTypeCode.setText(null);
        Optional.ofNullable(myMeta.getDeployment())
            .ifPresent(deployment -> {
                myDeploymentTable.setText(deployment.getTable());
                myTypeCode.setText(deployment.getTypeCode());
            });
    }

    public JBPanel getContent(final TSMetaRelation meta) {
        initData(meta);
        myTabs.setSelectedIndex(TAB_DETAILS_INDEX);

        return myContentPane;
    }

    public JBPanel getContent(final TSMetaRelation.TSMetaRelationElement meta) {
        initData(meta.getOwningRelation());

        if (meta.getEnd() == TSMetaRelation.RelationEnd.SOURCE) {
            myTabs.setSelectedIndex(TAB_SOURCE_INDEX);
        } else if (meta.getEnd() == TSMetaRelation.RelationEnd.TARGET) {
            myTabs.setSelectedIndex(TAB_TARGET_INDEX);
        }

        return myContentPane;
    }

    private void createUIComponents() {
        myCardinalitySource = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Cardinality.values())));
        myCardinalityTarget = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Cardinality.values())));

        myDetailsPane = new JBPanel();
        myDeploymentPane = new JBPanel<>();
        myFlagsPane = new JBPanel<>();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myDeploymentPane.setBorder(IdeBorderFactory.createTitledBorder("Deployment"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
    }
}