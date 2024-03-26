/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaEnum;
import com.intellij.idea.plugin.hybris.system.type.psi.TSPsiHelper;
import com.intellij.idea.plugin.hybris.toolwindow.system.type.components.TSMetaEnumValuesTable;
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

public class TSMetaEnumView {

    private final Project myProject;
    private TSGlobalMetaEnum myMeta;

    private JBPanel myContentPane;
    private JBTextField myJaloClass;
    private JBCheckBox myDynamic;
    private JBCheckBox myAutoCreate;
    private JBCheckBox myGenerate;
    private JBTextField myDescription;
    private JBTextField myCode;
    private JPanel myValuesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;
    private AbstractTable<TSGlobalMetaEnum, TSMetaEnum.TSMetaEnumValue> myEnumValues;

    public TSMetaEnumView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSGlobalMetaEnum myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myJaloClass.setText(myMeta.getJaloClass());
        myDynamic.setSelected(myMeta.isDynamic());
        myAutoCreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());

        myEnumValues.updateModel(myMeta);
    }

    public JBPanel getContent(final TSGlobalMetaEnum meta) {
        initData(meta);

        return myContentPane;
    }

    public JBPanel getContent(final TSGlobalMetaEnum meta, final TSMetaEnum.TSMetaEnumValue metaValue) {
        initData(meta);

        myEnumValues.select(metaValue);

        return myContentPane;
    }

    private void createUIComponents() {
        myEnumValues = TSMetaEnumValuesTable.getInstance(myProject);
        myValuesPane = ToolbarDecorator.createDecorator(myEnumValues)
            .setRemoveAction(anActionButton -> Optional.ofNullable(myEnumValues.getCurrentItem())
                .ifPresent(it -> TSPsiHelper.INSTANCE.delete(myProject, myMeta, it)))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myEnumValues.getCurrentItem())
                .map(TSMetaClassifier::isCustom)
                .orElse(false))
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myValuesPane.setBorder(IdeBorderFactory.createTitledBorder("Values"));

        PopupHandler.installPopupMenu(myEnumValues, "TSView.ToolWindow.TablePopup", "TSView.ToolWindow.TablePopup");
    }
}
