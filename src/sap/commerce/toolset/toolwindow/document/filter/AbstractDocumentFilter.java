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

package sap.commerce.toolset.toolwindow.document.filter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public abstract class AbstractDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr) throws BadLocationException {
        if (test(string)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException {
        if (test(text)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    public abstract boolean test(final String text);
}
