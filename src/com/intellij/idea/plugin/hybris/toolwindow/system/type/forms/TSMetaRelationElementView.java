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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.forms;

import com.intellij.idea.plugin.hybris.toolwindow.system.type.components.AbstractTSMetaCustomPropertiesTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.type.components.TSMetaRelationElementCustomPropertiesTable;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.model.Cardinality;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

public class TSMetaRelationElementView {

    private final Project myProject;
    private TSMetaClassifier<RelationElement> myMeta;

    private JBPanel myContentPane;
    private JBTextField myQualifier;
    private JTextPane myDescription;
    private JBCheckBox myPartOf;
    private JBCheckBox myEncrypted;
    private JBCheckBox myDoNotOptimize;
    private JBCheckBox myPrivate;
    private JBCheckBox myUnique;
    private JBCheckBox myInitial;
    private JBCheckBox myRead;
    private JBCheckBox myWrite;
    private JBCheckBox mySearch;
    private JBCheckBox myRemovable;
    private JBCheckBox myOptional;
    private JBCheckBox myOrdered;
    private JBCheckBox myNavigable;
    private JBTextField myMetaType;
    private JBTextField myType;
    private ComboBox<Cardinality> myCardinality;
    private ComboBox<Type> myCollectionType;
    private JPanel myCustomPropertiesPane;
    private JPanel myDetailsPane;
    private JPanel myFlagsPane;
    private JPanel myModifiersPane;
    private JBCheckBox myDeprecated;
    private AbstractTSMetaCustomPropertiesTable<TSMetaRelation.TSMetaRelationElement> myCustomProperties;

    public TSMetaRelationElementView(final Project project) {
        myProject = project;
    }

    public void updateView(final TSMetaRelation.TSMetaRelationElement myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myQualifier.setText(myMeta.getQualifier());
        myDescription.setText(myMeta.getDescription());
        myType.setText(myMeta.getType());
        myCardinality.setSelectedItem(myMeta.getCardinality());
        myCollectionType.setSelectedItem(myMeta.getCollectionType());
        myMetaType.setText(myMeta.getMetaType());
        myOrdered.setSelected(myMeta.isOrdered());
        myDeprecated.setSelected(myMeta.isDeprecated());
        myNavigable.setSelected(myMeta.isNavigable());

        final var modifiers = myMeta.getModifiers();
        myPartOf.setSelected(modifiers.isPartOf());
        myEncrypted.setSelected(modifiers.isEncrypted());
        myDoNotOptimize.setSelected(modifiers.isDoNotOptimize());
        myPrivate.setSelected(modifiers.isPrivate());
        myUnique.setSelected(modifiers.isUnique());
        myInitial.setSelected(modifiers.isInitial());
        myRead.setSelected(modifiers.isRead());
        myWrite.setSelected(modifiers.isWrite());
        mySearch.setSelected(modifiers.isSearch());
        myRemovable.setSelected(modifiers.isRemovable());
        myOptional.setSelected(modifiers.isOptional());

        myCustomProperties.updateModel(myMeta);
    }

    public JBPanel getContent() {
        return myContentPane;
    }

    private void createUIComponents() {
        myCardinality = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Cardinality.values())));
        myCollectionType = new ComboBox<>(new CollectionComboBoxModel<>(Arrays.asList(Type.values())));
        myCustomProperties = TSMetaRelationElementCustomPropertiesTable.Companion.getInstance(myProject);
        myDetailsPane = new JBPanel<>();
        myModifiersPane = new JBPanel<>();
        myFlagsPane = new JBPanel<>();

        myCustomPropertiesPane = ToolbarDecorator.createDecorator(myCustomProperties)
                                                 .disableUpDownActions()
                                                 .setPanelBorder(JBUI.Borders.empty())
                                                 .createPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myModifiersPane.setBorder(IdeBorderFactory.createTitledBorder("Modifiers"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myCustomPropertiesPane.setBorder(IdeBorderFactory.createTitledBorder("Custom Properties"));
    }
}
