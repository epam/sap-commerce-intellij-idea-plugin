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

package com.intellij.idea.plugin.hybris.toolwindow;

import com.intellij.idea.plugin.hybris.type.system.meta.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaProperty;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TSMetaClassView implements DumbAware {

    private JPanel contentPane;
    private JTextField myDescription;
    private JComboBox<String> myExtends;
    private JTextField myJaloClass;
    private JTextField myDeployment;
    private JTabbedPane myTabs;
    private JPanel myDetailsTab;
    private JPanel myAttributesTab;
    private JPanel myCustomPropertiesTab;
    private JTextField myCode;
    private JTable myAttributes;
    private JTable myCustomAttributes;
    private JCheckBox myAbstract;
    private JCheckBox myAutocreate;
    private JCheckBox mySingleton;
    private JCheckBox myJaloonly;
    private JCheckBox myGenerate;

    public static JPanel create(final Project project, final TSMetaClass source) {
        final TSMetaClassView view = new TSMetaClassView();
        final ItemType dom = source.retrieveDom();

        final ListTableModel<TSMetaProperty> attributesModel = new ListTableModel<>();
        final List<TSMetaProperty> attributes = source.getPropertiesStream(false)
                                                      .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                                                      .collect(Collectors.toList());
        attributesModel.setItems(attributes);
        final ColumnInfo[] columnInfos = Stream.of("code", "type", "redeclare", "defaultValue")
                                               .map(column -> new ColumnInfo<TSMetaProperty, String>(column) {

                                                   @Override
                                                   public @Nullable String valueOf(final TSMetaProperty s) {
                                                       final Attribute attribute = s.retrieveDom();

                                                       switch (getName()) {
                                                           case "code": return s.getName();
                                                           case "type": return s.getType();
                                                           case "redeclare": return String.valueOf(Boolean.TRUE.equals(attribute.getRedeclare().getValue()));
                                                           case "defaultValue": return attribute.getDefaultValue().getValue();
                                                           default: return null;
                                                       }
                                                   }
                                               })
                                               .collect(Collectors.toList())
                                               .toArray(new ColumnInfo[]{});
        attributesModel.setColumnInfos(columnInfos);

        final CollectionComboBoxModel<String> extendClasses = new CollectionComboBoxModel<>();
        final List<String> listItems = TSMetaModelService.Companion.getInstance(project).metaModel()
                                                                   .<TSMetaClass>getMetaType(MetaType.META_CLASS)
                                                                   .values().stream()
                                                                   .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                                                                   .filter(item -> !item.equals(source))
                                                                   .map(TSMetaClassifier::getName)
                                                                   .collect(Collectors.toList());
        extendClasses.add(listItems);
        view.myExtends.setModel(extendClasses);
        view.myExtends.setSelectedItem(source.getName());
        view.myAttributes.setModel(attributesModel);
        view.myCode.setText(dom.getExtends().getStringValue());
        view.myDescription.setText(dom.getDescription().toString());
        view.myJaloClass.setText(dom.getJaloclass().getStringValue());
        view.myDeployment.setText(dom.getDeploymentAttr().getStringValue());
        view.myAbstract.setSelected(Boolean.TRUE.equals(dom.getAbstract().getValue()));
        view.myAutocreate.setSelected(Boolean.TRUE.equals(dom.getAutoCreate().getValue()));
        view.myGenerate.setSelected(Boolean.TRUE.equals(dom.getGenerate().getValue()));
        view.mySingleton.setSelected(Boolean.TRUE.equals(dom.getSingleton().getValue()));
        view.myJaloonly.setSelected(Boolean.TRUE.equals(dom.getJaloOnly().getValue()));


        return view.contentPane;
    }

}
