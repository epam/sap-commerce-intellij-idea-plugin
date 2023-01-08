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

package com.intellij.idea.plugin.hybris.settings.forms;

import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.ui.JBEmptyBorder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

abstract class RemoteInstancesListPanel extends AddEditDeleteListPanel<HybrisRemoteConnectionSettings> {

    @Serial
    private static final long serialVersionUID = -1932103943790251488L;
    private ListCellRenderer myListCellRenderer;
    private boolean triggerSave = false;
    final Project myProject;

    abstract Icon getCellIcon();
    abstract void saveSettings();
    abstract void addItem();

    public RemoteInstancesListPanel(
        final Project project,
        final String title,
        final List<HybrisRemoteConnectionSettings> initialList
    ) {
        super(title, initialList);
        this.myProject = project;
        myList.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(final ListDataEvent e) {
                trySaveSettings();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                trySaveSettings();
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {
                trySaveSettings();
            }
        });
    }

    public void setInitialList(final List<HybrisRemoteConnectionSettings> remoteConnectionSettingsList) {
        myListModel.clear();
        remoteConnectionSettingsList.forEach(this::addElement);
        triggerSave = true;
    }

    public List<HybrisRemoteConnectionSettings> getData() {
        final List<HybrisRemoteConnectionSettings> remoteConnectionSettingsList = new ArrayList<>();
        for (int index = 0; index < myList.getModel().getSize(); index++) {
            remoteConnectionSettingsList.add(myList.getModel().getElementAt(index));
        }
        return remoteConnectionSettingsList;
    }

    @Nullable
    @Override
    protected HybrisRemoteConnectionSettings findItemToAdd() {
        return null;
    }

    @Override
    protected ListCellRenderer getListCellRenderer() {
        if (myListCellRenderer == null) {
            myListCellRenderer = new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                    final JList list,
                    final Object value,
                    final int index,
                    final boolean isSelected,
                    final boolean cellHasFocus
                ) {
                    final var comp = super.getListCellRendererComponent(list, value.toString(), index, isSelected, cellHasFocus);
                    ((JComponent) comp).setBorder(new JBEmptyBorder(5));

                    setIcon(getCellIcon());
                    return comp;
                }
            };
        }
        return myListCellRenderer;
    }

    @Override
    protected void customizeDecorator(final ToolbarDecorator decorator) {
        super.customizeDecorator(decorator);
        decorator.setAddAction(button -> addItem());
        decorator.setMoveUpAction(button -> ListUtil.moveSelectedItemsUp(myList));
        decorator.setMoveDownAction(button -> ListUtil.moveSelectedItemsDown(myList));
    }

    protected void trySaveSettings() {
        if (triggerSave) saveSettings();
    }

}
