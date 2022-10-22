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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCollection;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class TSMetaCollectionView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBCheckBox myAutoCreate;
    private JBCheckBox myGenerate;
    private ComboBox<Type> myType;
    private JBTextField myCode;
    private JBTextField myElementType;

    public TSMetaCollectionView(final Project project) {
        myProject = project;
    }

    private void initData(final TSMetaCollection myMeta) {
        if (StringUtils.equals(myMeta.getName(), myCode.getText())) {
            // same object, no need in re-init
            return;
        }

        myCode.setText(myMeta.getName());
        myType.setSelectedItem(myMeta.getType());
        myElementType.setText(myMeta.getElementType());
        myAutoCreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());
    }

    public JBPanel getContent(final TSMetaCollection meta) {
        initData(meta);

        return myContentPane;
    }

    private void createUIComponents() {
        final CollectionComboBoxModel<Type> myTypeModel = new CollectionComboBoxModel<>(Arrays.asList(Type.values()));
        myType = new ComboBox<>(myTypeModel);
    }
}
