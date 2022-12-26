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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms;

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaAnnotations;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaClassifier;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaHint;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaImport;
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaProperty;
import com.intellij.idea.plugin.hybris.system.bean.model.Bean;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaAnnotationsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaHintsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaImportsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaPropertiesTable;
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Objects;

public class BSMetaBeanView {

    private final Project myProject;
    private BSMetaClassifier<Bean> myMeta;
    private JPanel myContentPane;
    private JBTextField myDescription;
    private JBTextField myClass;
    private JBCheckBox myDeprecated;
    private JBTextField myTemplate;
    private JPanel myPropertiesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;
    private JBTextField myDeprecatedSince;
    private JBCheckBox mySuperEquals;
    private JPanel myHintsPane;
    private JBTextField myExtends;
    private JPanel myImportsPane;
    private JPanel myAnnotationsPane;
    private JBCheckBox myAbstract;
    private AbstractTable<BSGlobalMetaBean, BSMetaProperty> myProperties;
    private AbstractTable<BSGlobalMetaBean, BSMetaHint> myHints;
    private AbstractTable<BSGlobalMetaBean, BSMetaImport> myImports;
    private AbstractTable<BSGlobalMetaBean, BSMetaAnnotations> myAnnotations;

    public BSMetaBeanView(final Project project) {
        this.myProject = project;
    }

    private void initData(final BSGlobalMetaBean myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myClass.setText(myMeta.getName());
        myExtends.setText(myMeta.getExtends());
        myTemplate.setText(myMeta.getTemplate());
        myDescription.setText(myMeta.getDescription());
        myDeprecatedSince.setText(myMeta.getDeprecatedSince());
        myDeprecated.setSelected(myMeta.isDeprecated());
        myAbstract.setSelected(myMeta.isAbstract());
        mySuperEquals.setSelected(myMeta.isSuperEquals());

        myProperties.updateModel(myMeta);
        myHints.updateModel(myMeta);
        myImports.updateModel(myMeta);
        myAnnotations.updateModel(myMeta);
    }

    public JPanel getContent(final BSGlobalMetaBean meta) {
        initData(meta);

        return myContentPane;
    }

    public JPanel getContent(final BSGlobalMetaBean meta, final BSMetaProperty metaValue) {
        initData(meta);

        myProperties.select(metaValue);

        return myContentPane;
    }

    private void createUIComponents() {
        myProperties = BSMetaPropertiesTable.Companion.getInstance(myProject);
        myHints = BSMetaHintsTable.Companion.getInstance(myProject);
        myImports = BSMetaImportsTable.Companion.getInstance(myProject);
        myAnnotations = BSMetaAnnotationsTable.Companion.getInstance(myProject);

        myPropertiesPane = ToolbarDecorator.createDecorator(myProperties)
                                           .disableUpDownActions()
                                           .setPanelBorder(JBUI.Borders.empty())
                                           .createPanel();
        myHintsPane = ToolbarDecorator.createDecorator(myHints)
                                           .disableUpDownActions()
                                           .setPanelBorder(JBUI.Borders.empty())
                                           .createPanel();
        myImportsPane = ToolbarDecorator.createDecorator(myImports)
                                           .disableUpDownActions()
                                           .setPanelBorder(JBUI.Borders.empty())
                                           .createPanel();
        myAnnotationsPane = ToolbarDecorator.createDecorator(myAnnotations)
                                           .disableUpDownActions()
                                           .setPanelBorder(JBUI.Borders.empty())
                                           .createPanel();
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myPropertiesPane.setBorder(IdeBorderFactory.createTitledBorder("Properties"));
        myHintsPane.setBorder(IdeBorderFactory.createTitledBorder("Hints"));
        myImportsPane.setBorder(IdeBorderFactory.createTitledBorder("Imports"));
        myAnnotationsPane.setBorder(IdeBorderFactory.createTitledBorder("Annotations"));
    }
}
