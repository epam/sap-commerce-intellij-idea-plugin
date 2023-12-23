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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.listeners;

import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.HybrisConsoleQueryTextField;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HybrisConsoleQueryBodyDocumentListener implements DocumentListener {

    private final HybrisConsoleQueryTextField textField;
    private final JButton button;
    private final String placeholder;

    public HybrisConsoleQueryBodyDocumentListener(final JButton button, final HybrisConsoleQueryTextField textField, final String placeholder) {
        this.button = button;
        this.textField = textField;
        this.placeholder = placeholder;
    }

    @Override
    public void beforeDocumentChange(@NotNull final DocumentEvent event) {
        disableButtonIfTextIsEmpty(event.getDocument().getText(), button, textField);
    }

    @Override
    public void documentChanged(@NotNull final DocumentEvent event) {
        disableButtonIfTextIsEmpty(event.getDocument().getText(), button, textField);
    }

    private void disableButtonIfTextIsEmpty(final String text, final JButton button, final HybrisConsoleQueryTextField textField) {
        if (!text.trim().isEmpty() && !textField.getText().equals(placeholder)) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }
    }
}