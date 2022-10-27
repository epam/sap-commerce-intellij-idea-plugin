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

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaEnumValuesTable;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaEnum;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class TSMetaEnumView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTextField myJaloClass;
    private JBCheckBox myDynamic;
    private JBCheckBox myAutoCreate;
    private JBCheckBox myGenerate;
    private JBTextField myDescription;
    private JBTextField myCode;
    private JBTable myEnumValues;
    private JPanel myValuesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;

    public TSMetaEnumView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaEnum myMeta) {
        if (StringUtils.equals(myMeta.getName(), myCode.getText())) {
            // same object, no need in re-init
            return;
        }

        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myJaloClass.setText(myMeta.getJaloClass());
        myDynamic.setSelected(myMeta.isDynamic());
        myAutoCreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());

        ((TSMetaEnumValuesTable) myEnumValues).updateModel(myMeta);
    }

    public JBPanel getContent(final TSMetaEnum meta) {
        initData(meta);

        return myContentPane;
    }

    public JBPanel getContent(final TSMetaEnum meta, final TSMetaEnum.TSMetaEnumValue metaValue) {
        initData(meta);

        ((TSMetaEnumValuesTable) myEnumValues).select(metaValue);

        return myContentPane;
    }

    private void createUIComponents() {
        myEnumValues = TSMetaEnumValuesTable.Companion.getInstance(myProject);
        myValuesPane = ToolbarDecorator.createDecorator(myEnumValues)
                                       .disableUpDownActions()
                                       .setPanelBorder(JBUI.Borders.empty())
                                       .createPanel();
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myValuesPane.setBorder(IdeBorderFactory.createTitledBorder("Values"));
    }
}
