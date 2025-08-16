/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package sap.commerce.toolset.toolwindow.document.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class SimpleDocumentListener implements DocumentListener {

    public abstract void update(DocumentEvent e);

    @Override
    public void insertUpdate(final DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        update(e);
    }
}
