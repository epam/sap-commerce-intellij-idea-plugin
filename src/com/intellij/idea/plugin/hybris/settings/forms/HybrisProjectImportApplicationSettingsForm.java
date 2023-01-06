/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.settings.forms;

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AddEditDeleteListPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created 21:58 29 March 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class HybrisProjectImportApplicationSettingsForm {

    private JPanel mainPanel;
    private JPanel junkDirectoriesPanel;
    private JCheckBox groupModulesCheckBox;
    private JTextField groupCustomTextField;
    private JTextField groupNonHybrisTextField;
    private JTextField groupCustomUnusedTextField;
    private JTextField groupHybrisTextField;
    private JTextField groupHybrisUnusedTextField;
    private JTextField groupPlatformTextField;
    private JPanel typeSystemDiagramStopWords;
    private JPanel extensionsResourcesToExclude;
    private JPanel excludedFromIndexPanel;

    private MyListPanel junkListPanel;
    private MyListPanel tsdListPanel;
    private MyListPanel extensionsResourcesToExcludeListPanel;
    private MyListPanel excludedFromIndexListPanel;

    public void setData(final HybrisApplicationSettings data) {
        junkListPanel.setMyList(data.getJunkDirectoryList());
        tsdListPanel.setMyList(data.getTsdStopTypeList());
        extensionsResourcesToExcludeListPanel.setMyList(data.getExtensionsResourcesToExcludeList());
        groupModulesCheckBox.setSelected(data.isGroupModules());
        groupCustomTextField.setText(data.getGroupCustom());
        groupNonHybrisTextField.setText(data.getGroupNonHybris());
        groupCustomUnusedTextField.setText(data.getGroupOtherCustom());
        groupHybrisTextField.setText(data.getGroupHybris());
        groupHybrisUnusedTextField.setText(data.getGroupOtherHybris());
        groupPlatformTextField.setText(data.getGroupPlatform());
        excludedFromIndexListPanel.setMyList(data.getExcludedFromIndexList());
    }

    public void getData(final HybrisApplicationSettings data) {
        data.setJunkDirectoryList(junkListPanel.getMyList());
        data.setTsdStopTypeList(tsdListPanel.getMyList());
        data.setExtensionsResourcesToExcludeList(extensionsResourcesToExcludeListPanel.getMyList());
        data.setGroupModules(groupModulesCheckBox.isSelected());
        data.setGroupCustom(groupCustomTextField.getText());
        data.setGroupOtherCustom(groupCustomUnusedTextField.getText());
        data.setGroupHybris(groupHybrisTextField.getText());
        data.setGroupOtherHybris(groupHybrisUnusedTextField.getText());
        data.setGroupNonHybris(groupNonHybrisTextField.getText());
        data.setGroupPlatform(groupPlatformTextField.getText());
        data.setExcludedFromIndexList(excludedFromIndexListPanel.getMyList());
    }

    public boolean isModified(final HybrisApplicationSettings data) {
        if (!junkListPanel.getMyList().equals(data.getJunkDirectoryList())) {
            return true;
        }
        if (!excludedFromIndexListPanel.getMyList().equals(data.getExcludedFromIndexList())) {
            return true;
        }
        if (!tsdListPanel.getMyList().equals(data.getTsdStopTypeList())) {
            return true;
        }
        if (!extensionsResourcesToExcludeListPanel.getMyList().equals(data.getExtensionsResourcesToExcludeList())) {
            return true;
        }
        if (groupModulesCheckBox.isSelected() != data.isGroupModules()) {
            return true;
        }
        if (!StringUtil.equals(groupCustomTextField.getText(), data.getGroupCustom())) {
            return true;
        }
        if (!StringUtil.equals(groupCustomUnusedTextField.getText(), data.getGroupOtherCustom())) {
            return true;
        }
        if (!StringUtil.equals(groupHybrisTextField.getText(), data.getGroupHybris())) {
            return true;
        }
        if (!StringUtil.equals(groupHybrisUnusedTextField.getText(), data.getGroupOtherHybris())) {
            return true;
        }
        if (!StringUtil.equals(groupPlatformTextField.getText(), data.getGroupPlatform())) {
            return true;
        }
        if (!StringUtil.equals(groupNonHybrisTextField.getText(), data.getGroupNonHybris())) {
            return true;
        }
        return false;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        junkListPanel = new MyListPanel("hybris.import.settings.junk.directory.name", "hybris.import.settings.junk.directory.popup.add.title", "hybris.import.settings.junk.directory.popup.add.text", "hybris.import.settings.junk.directory.popup.edit.title", "hybris.import.settings.junk.directory.popup.edit.text", new ArrayList<String>());
        junkDirectoriesPanel = junkListPanel;
        tsdListPanel = new MyListPanel("hybris.import.settings.tsv.diagram.name", "hybris.import.settings.tsv.diagram.popup.add.title", "hybris.import.settings.tsv.diagram.popup.add.text", "hybris.import.settings.tsv.diagram.popup.edit.title", "hybris.import.settings.tsv.diagram.popup.edit.text", new ArrayList<String>());
        typeSystemDiagramStopWords = tsdListPanel;
        extensionsResourcesToExcludeListPanel = new MyListPanel("hybris.import.settings.exclude.resources.name", "hybris.import.settings.exclude.resources.popup.add.title", "hybris.import.settings.exclude.resources.popup.add.text", "hybris.import.settings.exclude.resources.popup.edit.title", "hybris.import.settings.exclude.resources.popup.edit.text", new ArrayList<String>());
        extensionsResourcesToExclude = extensionsResourcesToExcludeListPanel;
        excludedFromIndexListPanel = new MyListPanel("hybris.import.settings.excludedFromIndex.directory.name", "hybris.import.settings.excludedFromIndex.directory.popup.add.title", "hybris.import.settings.excludedFromIndex.directory.popup.add.text", "hybris.import.settings.excludedFromIndex.directory.popup.edit.title", "hybris.import.settings.excludedFromIndex.directory.popup.edit.text", new ArrayList<String>());
        excludedFromIndexPanel = excludedFromIndexListPanel;
    }

    public void createComponent() {
        groupModulesCheckBox.addChangeListener(changeEvent -> {
            groupCustomTextField.setEnabled(groupModulesCheckBox.isSelected());
            groupCustomUnusedTextField.setEnabled(groupModulesCheckBox.isSelected());
            groupHybrisTextField.setEnabled(groupModulesCheckBox.isSelected());
            groupHybrisUnusedTextField.setEnabled(groupModulesCheckBox.isSelected());
            groupPlatformTextField.setEnabled(groupModulesCheckBox.isSelected());
            groupNonHybrisTextField.setEnabled(groupModulesCheckBox.isSelected());
        });
    }

    private static class MyListPanel extends AddEditDeleteListPanel<String> {
        @Serial
        private static final long serialVersionUID = -6339262026248471671L;
        private final String addTitle;
        private final String addText;
        private final String editTitle;
        private final String editText;

        public MyListPanel(final String title, final String addTitle, final String addText, final String editTitle, final String editText, final List<String> initialList) {
            super(HybrisI18NBundleUtils.message(title), initialList);
            this.addTitle = addTitle;
            this.addText = addText;
            this.editTitle = editTitle;
            this.editText = editText;
        }

        public void setMyList(@Nullable java.util.List<String> itemList) {
            myListModel.clear();
            for (String itemToAdd : itemList) {
                super.addElement(itemToAdd);
            }
        }

        @Nullable
        @Override
        protected String findItemToAdd() {
            return showEditDialog(
                "",
                HybrisI18NBundleUtils.message(addTitle),
                HybrisI18NBundleUtils.message(addText)
            );
        }

        @Nullable
        @Override
        protected String editSelectedItem(@NotNull final String item) {
            return showEditDialog(
                item,
                HybrisI18NBundleUtils.message(editTitle),
                HybrisI18NBundleUtils.message(editText)
            );
        }

        @Nullable
        private String showEditDialog(
            @NotNull final String initialValue,
            @NotNull final String title,
            @NotNull final String message
        ) {
            return Messages.showInputDialog(
                this,
                message,
                title,
                Messages.getQuestionIcon(),
                initialValue,
                new InputValidatorEx() {

                    @Override
                    public boolean checkInput(@NotNull String inputString) {
                        return !StringUtil.isEmpty(inputString);
                    }

                    @Override
                    public boolean canClose(@NotNull String inputString) {
                        return !StringUtil.isEmpty(inputString) && (!myListModel.contains(inputString) || initialValue.equals(
                            inputString));
                    }

                    @Nullable
                    @Override
                    public String getErrorText(@NotNull String inputString) {
                        if (!checkInput(inputString)) {
                            return "directory name string cannot be empty";
                        }
                        if (!canClose(inputString)) {
                            return "duplicities are not allowed (nor make any sense)";
                        }
                        return null;
                    }
                }
            );
        }

        @NotNull
        public java.util.List<String> getMyList() {
            return Collections.list(myListModel.elements());
        }
    }
}
