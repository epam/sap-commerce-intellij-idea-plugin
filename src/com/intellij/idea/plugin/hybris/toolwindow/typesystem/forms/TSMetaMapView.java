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

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaMap;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.model.MapType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.util.Objects;

public class TSMetaMapView {

    private final Project myProject;
    private TSMetaClassifier<MapType> myMeta;

    private JBPanel myContentPane;
    private JBCheckBox myAutocreate;
    private JBCheckBox myGenerate;
    private JBCheckBox myRedeclare;
    private JBTextField myCode;
    private JBTextField myArgumentType;
    private JBTextField myReturnType;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;

    public TSMetaMapView(final Project project) {
        myProject = project;
    }

    private void initData(final TSGlobalMetaMap myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myCode.setText(myMeta.getName());
        myReturnType.setText(myMeta.getReturnType());
        myArgumentType.setText(myMeta.getArgumentType());
        myAutocreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());
        myRedeclare.setSelected(myMeta.isRedeclare());
    }

    public JBPanel getContent(final TSGlobalMetaMap meta) {
        initData(meta);

        return myContentPane;
    }

    private void createUIComponents() {
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();
        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
    }
}
