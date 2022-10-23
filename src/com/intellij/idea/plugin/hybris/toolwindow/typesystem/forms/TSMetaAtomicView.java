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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAtomic;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class TSMetaAtomicView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBCheckBox myAutoCreate;
    private JBCheckBox myGenerate;
    private JBTextField myClass;
    private JBTextField myExtends;
    private JPanel myFlagsPane;
    private JBPanel myDetailsPane;

    public TSMetaAtomicView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaAtomic myMeta) {
        if (StringUtils.equals(myMeta.getName(), myClass.getText())) {
            // same object, no need in re-init
            return;
        }

        myClass.setText(myMeta.getName());
        myAutoCreate.setSelected(myMeta.isAutocreate());
        myGenerate.setSelected(myMeta.isGenerate());
        myExtends.setText(myMeta.getExtends());
    }

    public JBPanel getContent(final TSMetaAtomic meta) {
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
