/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.exec.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.ui.JBEmptyBorder;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState;
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsStateKt;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

abstract public class RemoteInstancesListPanel<T extends ExecConnectionSettingsState> extends AddEditDeleteListPanel<T> {

    @Serial
    private static final long serialVersionUID = -1932103943790251488L;
    private ListCellRenderer myListCellRenderer;
    private final Icon icon;
    protected final Project myProject;

    public abstract void addItem();

    public enum EventType {
        ADD, REMOVE, CHANGE
    }

    public RemoteInstancesListPanel(final Project project, final Icon icon) {
        super(null, Collections.emptyList());
        this.myProject = project;
        this.icon = icon;
        myList.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(final ListDataEvent e) {
                onDataChanged(EventType.ADD, getData());
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                onDataChanged(EventType.REMOVE, getData());
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {
                onDataChanged(EventType.CHANGE, getData());
            }
        });
    }

    public void setData(final Collection<T> remoteConnectionSettingsList) {
        myListModel.clear();
        myListModel.addAll(remoteConnectionSettingsList);
    }

    public Set<T> getData() {
        final var remoteConnectionSettingsList = new LinkedHashSet<T>();
        for (int index = 0; index < myList.getModel().getSize(); index++) {
            remoteConnectionSettingsList.add(myList.getModel().getElementAt(index));
        }
        return remoteConnectionSettingsList;
    }

    abstract protected void onDataChanged(EventType eventType, final Set<T> data);

    @Nullable
    @Override
    protected T findItemToAdd() {
        return null;
    }

    @Override
    protected void addElement(@Nullable final T itemToAdd) {
        super.addElement(itemToAdd);
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
                    final var name = value instanceof ExecConnectionSettingsState connection
                        ? ExecConnectionSettingsStateKt.getPresentationName(connection)
                        : value.toString();

                    final var comp = super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                    ((JComponent) comp).setBorder(new JBEmptyBorder(5));
                    setIcon(RemoteInstancesListPanel.this.icon);
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
//        decorator.setMoveUpAction(button -> ListUtil.moveSelectedItemsUp(myList));
//        decorator.setMoveDownAction(button -> ListUtil.moveSelectedItemsDown(myList));
        decorator.setRemoveActionUpdater(e -> myListModel.size() > 1 && myListModel.size() != myList.getSelectedIndices().length);
    }

}
