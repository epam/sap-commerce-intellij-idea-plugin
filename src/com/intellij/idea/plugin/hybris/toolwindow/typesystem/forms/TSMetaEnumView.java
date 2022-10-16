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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

public class TSMetaEnumView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTextField myJaloClass;
    private JBCheckBox myDynamic;
    private JBCheckBox myAutocreate;
    private JBCheckBox myGenerate;
    private JBTextField myDescription;
    private JBTextField myCode;

    public TSMetaEnumView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSMetaEnum myMeta) {
        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myJaloClass.setText(myMeta.getJaloclass());
        myDynamic.setSelected(myMeta.isDynamic());
        myAutocreate.setSelected(myMeta.isAutocreate());
        myGenerate.setSelected(myMeta.isGenerate());
    }

    public JBPanel getContent(final TSMetaEnum meta) {
        initData(meta);

        return myContentPane;
    }
}
