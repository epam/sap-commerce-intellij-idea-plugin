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

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;

public class TSMetaCustomPropertyView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTextField myJaloClass;
    private JTextPane myDescription;
    private JBTextField myExtends;
    private JBTextField myCode;
    private JBCheckBox myGenerate;
    private JBCheckBox myAbstract;
    private JBCheckBox myAutoCreate;
    private JBCheckBox mySingleton;
    private JBCheckBox myJaloOnly;

    public TSMetaCustomPropertyView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaCustomProperty myMeta) {
    }

    public JBPanel getContent(final TSMetaCustomProperty meta) {
        initData(meta);

        return myContentPane;
    }
}
