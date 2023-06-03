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

package com.intellij.idea.plugin.hybris.impex.tableFormatting;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.jetbrains.annotations.NotNull;

public class ImpexTableEditor {

    protected final Editor editor;

    public ImpexTableEditor(Editor editor) {
        this.editor = editor;
    }

    private SelectionModel getSelectionModel() {
        return editor.getSelectionModel();
    }

    public String getText() {
        return editor.getDocument().getText();
    }


    public void setSelection(final com.intellij.util.Range<Integer> range) {
        getSelectionModel().setSelection(range.getFrom(), range.getTo());
    }

    @NotNull
    public Editor getIdeaEditor() {
        return editor;
    }
}
