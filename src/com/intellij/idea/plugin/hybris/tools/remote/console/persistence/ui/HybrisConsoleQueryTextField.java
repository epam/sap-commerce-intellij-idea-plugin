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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui;

import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HybrisConsoleQueryTextField extends JTextField {

    private final Color placeholderForeground = Gray._160;
    private boolean textWrittenIn;
    private final String reservedPlaceholderValue;

    public HybrisConsoleQueryTextField(final String reservedPlaceholderValue) {
        this.reservedPlaceholderValue = reservedPlaceholderValue;
    }

    public Color getPlaceholderForeground() {
        return placeholderForeground;
    }

    public boolean isTextWrittenIn() {
        return textWrittenIn;
    }

    public void setTextWrittenIn(final boolean textWrittenIn) {
        this.textWrittenIn = textWrittenIn;
    }

    public void setPlaceholder(final String placeholder, final HybrisConsole hybrisConsole, final JButton button) {
        button.setEnabled(false);
        this.customizeText(placeholder);
        this.getDocument().addDocumentListener(new QueryTextFieldDocumentListener(hybrisConsole, button));
        this.addFocusListener(new QueryTextFieldFocusListener(placeholder, button));
    }

    private void customizeText(final String text) {
        setText(text);
        setFont(new Font(getFont().getFamily(), Font.ITALIC, getFont().getSize()));
        setForeground(getPlaceholderForeground());
        setTextWrittenIn(false);
    }

    private void setUsualTextDesign() {
        setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
        setForeground(JBColor.BLACK);
    }

    private class QueryTextFieldDocumentListener implements DocumentListener {

        private final HybrisConsole hybrisConsole;
        private final JButton button;

        public QueryTextFieldDocumentListener(final HybrisConsole hybrisConsole, final JButton button) {
            this.hybrisConsole = hybrisConsole;
            this.button = button;
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            disableButtonIfTextIsEmpty();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            disableButtonIfTextIsEmpty();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            disableButtonIfTextIsEmpty();
        }

        private void disableButtonIfTextIsEmpty() {
            if (!getText().trim().isEmpty() && !getText().equals(reservedPlaceholderValue)
                && !hybrisConsole.getEditorDocument().getText().isEmpty()) {
                setUsualTextDesign();
                setTextWrittenIn(true);
                button.setEnabled(true);
            } else if (hybrisConsole.getEditorDocument().getText().isEmpty()) {
                setUsualTextDesign();
                button.setEnabled(false);
            } else {
                button.setEnabled(false);
            }
        }
    }

    private class QueryTextFieldFocusListener implements FocusListener {

        private final String placeholder;
        private final JButton button;

        public QueryTextFieldFocusListener(final String placeholder, final JButton button) {
            this.placeholder = placeholder;
            this.button = button;
        }

        @Override
        public void focusGained(final FocusEvent e) {
            if (!isTextWrittenIn()) {
                setText("");
            }
        }

        @Override
        public void focusLost(final FocusEvent e) {
            if (getText().trim().isEmpty()) {
                customizeText(placeholder);
                button.setEnabled(false);
            }
        }
    }
}