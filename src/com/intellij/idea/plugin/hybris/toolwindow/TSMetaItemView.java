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

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.forms.TSMetaItemViewDataSupplier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.util.Optional;

public class TSMetaItemView {

    private static final Key<Integer> ACTIVE_TAB_INDEX = Key.create("TSMETAITEMVIEW_ACTIVE_INDEX");
    private JBPanel contentPane;
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
    private JBPanel myDetailsContent;
    private JBTextArea myDescription;
    private JBTabbedPane myTabs;

    public static JPanel create(final Project project, final TSMetaItem source) {
        final TSMetaItemView view = new TSMetaItemView();

        final ItemType dom = source.retrieveDom();

        TSMetaItemViewDataSupplier.Companion.getInstance(project).initAttributesTable(view.myAttributes, source);
        TSMetaItemViewDataSupplier.Companion.getInstance(project).initExtends(view.myExtends, source);

        view.myCode.setText(dom.getCode().getStringValue());
        Optional.ofNullable(dom.getDescription().getXmlTag())
                .map(description -> description.getValue().getText())
                .ifPresent(text -> view.myDescription.setText(text));
        view.myJaloClass.setText(dom.getJaloclass().getStringValue());
        view.myDeploymentTable.setText(dom.getDeployment().getTable().getStringValue());
        view.myDeploymentTypeCode.setText(dom.getDeployment().getTypeCode().getStringValue());
        view.myAbstract.setSelected(Boolean.TRUE.equals(dom.getAbstract().getValue()));
        view.myAutocreate.setSelected(Boolean.TRUE.equals(dom.getAutoCreate().getValue()));
        view.myGenerate.setSelected(Boolean.TRUE.equals(dom.getGenerate().getValue()));
        view.mySingleton.setSelected(Boolean.TRUE.equals(dom.getSingleton().getValue()));
        view.myJaloonly.setSelected(Boolean.TRUE.equals(dom.getJaloOnly().getValue()));

        Optional.ofNullable(project.<Integer>getUserData(ACTIVE_TAB_INDEX))
                .ifPresent(previouslySelectedIndex -> view.myTabs.setSelectedIndex(previouslySelectedIndex));
        view.myTabs.addChangeListener(e -> {
            final Object source1 = e.getSource();
            if (source1 instanceof JTabbedPane) {
                project.putUserData(ACTIVE_TAB_INDEX, ((JTabbedPane) source1).getSelectedIndex());
            }
        });

        return view.contentPane;
    }

}
