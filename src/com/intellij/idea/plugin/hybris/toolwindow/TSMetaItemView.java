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

package com.intellij.idea.plugin.hybris.toolwindow;

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemAttributesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemExtendsCombobox;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemTabbedPane;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.util.Optional;

public class TSMetaItemView {

    private final Project myProject;
    private final TSMetaItem myMeta;

    private JBPanel myContentPane;
    private JComboBox<String> myExtends;
    private JBTextField myJaloClass;
    private JBTextField myDeploymentTable;
    private JBTextField myDeploymentTypeCode;
    private JBTextField myCode;
    private JBTable myAttributes;
    private JBTable myCustomAttributes;
    private JBCheckBox myAbstract;
    private JBCheckBox myAutocreate;
    private JBCheckBox mySingleton;
    private JBCheckBox myJaloonly;
    private JBCheckBox myGenerate;
    private JBTextArea myDescription;
    private JBTabbedPane myTabs;

    public TSMetaItemView(final Project project, final TSMetaItem meta) {
        this.myProject = project;
        this.myMeta = meta;

        initData();
    }

    private void initData() {
        final ItemType dom = myMeta.retrieveDom();

        ((TSMetaItemTabbedPane) myTabs).init();

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
    }

    public JBPanel getContent() {
        return myContentPane;
    }

    private void createUIComponents() {
        myAttributes = new TSMetaItemAttributesTable(myProject, myMeta);
        myExtends = new TSMetaItemExtendsCombobox(myProject, myMeta);
        myTabs = new TSMetaItemTabbedPane(myProject, myMeta);
    }
}
