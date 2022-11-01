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

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemAttributesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemCustomPropertiesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaItemIndexesTable;
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSMetaRelationElementsTable;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class TSMetaItemView {

    private final Project myProject;

    private JBPanel myContentPane;
    private JBTextField myExtends;
    private JBTextField myJaloClass;
    private JBTextField myDeploymentTable;
    private JBTextField myDeploymentTypeCode;
    private JBTextField myCode;
    private TSMetaItemAttributesTable myAttributes;
    private TSMetaItemCustomPropertiesTable myCustomProperties;
    private TSMetaItemIndexesTable myIndexes;
    private TSMetaRelationElementsTable myRelations;
    private JBCheckBox myAbstract;
    private JBCheckBox myAutoCreate;
    private JBCheckBox mySingleton;
    private JBCheckBox myJaloOnly;
    private JBCheckBox myGenerate;
    private JTextPane myDescription;
    private JBScrollPane myScrollablePane;
    private JPanel myIndexesPane;
    private JPanel myCustomPropertiesPane;
    private JPanel myAttributesPane;
    private JBPanel myDetailsPane;
    private JPanel myDeploymentPane;
    private JPanel myFlagsPane;
    private JPanel myRelationsPane;
    private JBCheckBox myCatalogAware;

    public TSMetaItemView(final Project project) {
        this.myProject = project;
    }

    private void initData(final TSGlobalMetaItem myMeta) {
        if (StringUtils.equals(myMeta.getName(), myCode.getText())) {
            // same object, no need in re-init
            return;
        }

        myAttributes.updateModel(myMeta);
        myCustomProperties.updateModel(myMeta);
        myIndexes.updateModel(myMeta);
        myRelations.updateModel(myMeta);

        myCode.setText(myMeta.getName());
        myDescription.setText(myMeta.getDescription());
        myJaloClass.setText(myMeta.getJaloClass());
        myDeploymentTable.setText(myMeta.getDeployment().getTable());
        myDeploymentTypeCode.setText(myMeta.getDeployment().getTypeCode());
        myAbstract.setSelected(myMeta.isAbstract());
        myCatalogAware.setSelected(myMeta.isCatalogAware());
        myAutoCreate.setSelected(myMeta.isAutoCreate());
        myGenerate.setSelected(myMeta.isGenerate());
        mySingleton.setSelected(myMeta.isSingleton());
        myJaloOnly.setSelected(myMeta.isJaloOnly());
        myExtends.setText(myMeta.getExtendedMetaItemName());
    }

    public JBPanel getContent(final TSGlobalMetaItem meta) {
        initData(meta);

        myScrollablePane.getVerticalScrollBar().setValue(0);

        return myContentPane;
    }

    public JBPanel getContent(final TSGlobalMetaItem meta, final TSMetaItem.TSMetaItemIndex metaIndex) {
        initData(meta);

        myIndexes.select(metaIndex);
        myScrollablePane.getVerticalScrollBar().setValue(myIndexesPane.getLocation().y);

        return myContentPane;
    }

    public JBPanel getContent(final TSGlobalMetaItem meta, final TSMetaItem.TSMetaItemAttribute metaAttribute) {
        initData(meta);

        myAttributes.select(metaAttribute);
        myScrollablePane.getVerticalScrollBar().setValue(myAttributesPane.getLocation().y);

        return myContentPane;
    }

    public JBPanel getContent(final TSGlobalMetaItem meta, final TSMetaCustomProperty metaCustomProperty) {
        initData(meta);

        myCustomProperties.select(metaCustomProperty);
        myScrollablePane.getVerticalScrollBar().setValue(myCustomPropertiesPane.getLocation().y);

        return myContentPane;
    }

    private void createUIComponents() {
        myAttributes = TSMetaItemAttributesTable.Companion.getInstance(myProject);
        myCustomProperties = TSMetaItemCustomPropertiesTable.Companion.getInstance(myProject);
        myIndexes = TSMetaItemIndexesTable.Companion.getInstance(myProject);
        myRelations = TSMetaRelationElementsTable.Companion.getInstance(myProject);
        myDetailsPane = new JBPanel();
        myDeploymentPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myAttributesPane = ToolbarDecorator.createDecorator(myAttributes)
                                           .disableUpDownActions()
                                           .setPanelBorder(JBUI.Borders.empty())
                                           .createPanel();
        myCustomPropertiesPane = ToolbarDecorator.createDecorator(myCustomProperties)
                                                 .disableUpDownActions()
                                                 .setPanelBorder(JBUI.Borders.empty())
                                                 .createPanel();
        myIndexesPane = ToolbarDecorator.createDecorator(myIndexes)
                                        .disableUpDownActions()
                                        .setPanelBorder(JBUI.Borders.empty())
                                        .createPanel();
        myRelationsPane = ToolbarDecorator.createDecorator(myRelations)
                                          .disableUpDownActions()
                                          .setPanelBorder(JBUI.Borders.empty())
                                          .createPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myDeploymentPane.setBorder(IdeBorderFactory.createTitledBorder("Deployment"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myAttributesPane.setBorder(IdeBorderFactory.createTitledBorder("Attributes"));
        myCustomPropertiesPane.setBorder(IdeBorderFactory.createTitledBorder("Custom Properties"));
        myIndexesPane.setBorder(IdeBorderFactory.createTitledBorder("Indexes"));
        myRelationsPane.setBorder(IdeBorderFactory.createTitledBorder("Relations"));
    }
}
