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

package com.intellij.idea.plugin.hybris.impex.view;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class ImpexStructureViewModel extends TextEditorBasedStructureViewModel
    implements StructureViewModel.ElementInfoProvider {

    protected ImpexStructureViewModel(final Editor editor, final ImpexFile file) {
        super(editor, file);
    }

    protected ImpexStructureViewModel(final ImpexFile file) {
        super(file);
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return new ImpexRootTreeElement((ImpexFile) getPsiFile());
    }

    @Override
    public boolean isAlwaysShowsPlus(final StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(final StructureViewTreeElement element) {
        return false;
    }
}
