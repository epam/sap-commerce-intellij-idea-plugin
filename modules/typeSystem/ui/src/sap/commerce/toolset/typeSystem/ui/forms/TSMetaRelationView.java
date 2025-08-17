/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.typeSystem.ui.forms;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import sap.commerce.toolset.HybrisIcons;
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaRelation;
import sap.commerce.toolset.typeSystem.meta.model.TSMetaClassifier;
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation;
import sap.commerce.toolset.typeSystem.model.Cardinality;
import sap.commerce.toolset.typeSystem.model.Relation;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

public class TSMetaRelationView {

    private static final int TAB_DETAILS_INDEX = 0;
    private static final int TAB_SOURCE_INDEX = 1;
    private static final int TAB_TARGET_INDEX = 2;
    private final Project myProject;
    private TSMetaClassifier<Relation> myMeta;

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

        myTabs.insertTab("Source", HybrisIcons.TypeSystem.INSTANCE.getRELATION_SOURCE(), mySourceView.getContent(), null, TAB_SOURCE_INDEX);
        myTabs.insertTab("Target", HybrisIcons.TypeSystem.INSTANCE.getRELATION_TARGET(), myTargetView.getContent(), null, TAB_TARGET_INDEX);
    }

    private void initData(final TSMetaRelation myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

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

        final var deployment = myMeta.getDeployment();
        if (deployment != null) {
            myDeploymentTable.setText(deployment.getTable());
            myTypeCode.setText(deployment.getTypeCode());
        } else {
            myDeploymentTable.setText(null);
            myTypeCode.setText(null);
        }
    }

    public JBPanel getContent(final TSGlobalMetaRelation meta) {
        initData(meta);
        myTabs.setSelectedIndex(TAB_DETAILS_INDEX);

        return myContentPane;
    }

    public JBPanel getContent(final TSGlobalMetaRelation.TSMetaRelationElement meta) {
        initData(meta.getOwner());

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