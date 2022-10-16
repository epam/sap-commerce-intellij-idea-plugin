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

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemAttributesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemCustomPropertiesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemIndexesTable;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;

import javax.swing.*;

public class TSMetaItemView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTextField myExtends;
    private JBTextField myJaloClass;
    private JBTextField myDeploymentTable;
    private JBTextField myDeploymentTypeCode;
    private JBTextField myCode;
    private JBTable myAttributes;
    private JBTable myCustomProperties;
    private JBTable myIndexes;
    private JBCheckBox myAbstract;
    private JBCheckBox myAutoCreate;
    private JBCheckBox mySingleton;
    private JBCheckBox myJaloOnly;
    private JBCheckBox myGenerate;
    private JTextPane myDescription;

    public TSMetaItemView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaItem myMeta) {
        ((TSMetaItemAttributesTable) myAttributes).init(myProject, myMeta);
        ((TSMetaItemCustomPropertiesTable) myCustomProperties).init(myProject, myMeta);
        ((TSMetaItemIndexesTable) myIndexes).init(myProject, myMeta);

        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myJaloClass.setText(myMeta.getJaloClass());
        myDeploymentTable.setText(myMeta.getDeployment().getTable());
        myDeploymentTypeCode.setText(myMeta.getDeployment().getTypeCode());
        myAbstract.setSelected(myMeta.isAbstract());
        myAutoCreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());
        mySingleton.setSelected(myMeta.isSingleton());
        myJaloOnly.setSelected(myMeta.isJaloOnly());
        myExtends.setText(myMeta.getExtendedMetaItemName());
    }

    public JBPanel getContent(final TSMetaItem meta) {
        initData(meta);

        return myContentPane;
    }

    private void createUIComponents() {
        myAttributes = new TSMetaItemAttributesTable();
        myCustomProperties = new TSMetaItemCustomPropertiesTable();
        myIndexes = new TSMetaItemIndexesTable();
    }
}
