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
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.util.Optional;

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
    private JBCheckBox myAutocreate;
    private JBCheckBox mySingleton;
    private JBCheckBox myJaloonly;
    private JBCheckBox myGenerate;
    private JTextPane myDescription;

    public TSMetaItemView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaItem myMeta) {
        final ItemType dom = myMeta.retrieveDom();

        ((TSMetaItemAttributesTable) myAttributes).init(myProject, myMeta);
        ((TSMetaItemCustomPropertiesTable) myCustomProperties).init(myProject, myMeta);
        ((TSMetaItemIndexesTable) myIndexes).init(myProject, myMeta);

        myCode.setText(dom.getCode().getStringValue());
        Optional.ofNullable(dom.getDescription().getXmlTag())
                .map(description -> description.getValue().getText())
                .ifPresent(text -> myDescription.setText(text));
        myJaloClass.setText(dom.getJaloclass().getStringValue());
        myDeploymentTable.setText(dom.getDeployment().getTable().getStringValue());
        myDeploymentTypeCode.setText(dom.getDeployment().getTypeCode().getStringValue());
        myAbstract.setSelected(Boolean.TRUE.equals(dom.getAbstract().getValue()));
        myAutocreate.setSelected(Boolean.TRUE.equals(dom.getAutoCreate().getValue()));
        myGenerate.setSelected(Boolean.TRUE.equals(dom.getGenerate().getValue()));
        mySingleton.setSelected(Boolean.TRUE.equals(dom.getSingleton().getValue()));
        myJaloonly.setSelected(Boolean.TRUE.equals(dom.getJaloOnly().getValue()));
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
