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

package com.intellij.idea.plugin.hybris.toolwindow.beans.forms;

import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaEnum;
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaClassifier;
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum;
import com.intellij.idea.plugin.hybris.beans.model.Enum;
import com.intellij.idea.plugin.hybris.toolwindow.beans.components.BeansMetaEnumValuesTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Objects;

public class BeansMetaEnumView {

    private final Project myProject;
    private BeansMetaClassifier<Enum> myMeta;
    private JPanel myContentPane;
    private JBTextField myDescription;
    private JBTextField myClass;
    private JBCheckBox myDeprecated;
    private JBTextField myTemplate;
    private JPanel myValuesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;
    private JBTextField myDeprecatedSince;
    private BeansMetaEnumValuesTable myEnumValues;

    public BeansMetaEnumView(final Project project) {
        this.myProject = project;
    }

    private void initData(final BeansGlobalMetaEnum myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myClass.setText(myMeta.getName());
        myTemplate.setText(myMeta.getTemplate());
        myDescription.setText(myMeta.getDescription());
        myDeprecatedSince.setText(myMeta.getDeprecatedSince());
        myDeprecated.setSelected(myMeta.isDeprecated());

        myEnumValues.updateModel(myMeta);
    }

    public JPanel getContent(final BeansGlobalMetaEnum meta) {
        initData(meta);

        return myContentPane;
    }

    public JPanel getContent(final BeansGlobalMetaEnum meta, final BeansMetaEnum.BeansMetaEnumValue metaValue) {
        initData(meta);

        myEnumValues.select(metaValue);

        return myContentPane;
    }

    private void createUIComponents() {
        myEnumValues = BeansMetaEnumValuesTable.Companion.getInstance(myProject);
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
