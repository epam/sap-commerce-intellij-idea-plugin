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

package com.intellij.idea.plugin.hybris.system.type.structure.view;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Function;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.structure.DomStructureViewBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/2/17.
 */
public class TSStructureViewBuilder extends DomStructureViewBuilder {

    private final Function<DomElement, DomService.StructureViewMode> myDescriptor;
    private final XmlFile myFile;

    public TSStructureViewBuilder(
        final XmlFile file,
        final Function<DomElement, DomService.StructureViewMode> descriptor
    ) {
        super(file, descriptor);
        myFile = file;
        myDescriptor = descriptor;
    }

    @NotNull
    @Override
    public StructureViewModel createStructureViewModel(@Nullable final Editor editor) {
        return new TSStructureViewTreeModel(myFile, myDescriptor, editor);
    }

    @Override
    @NotNull
    public StructureView createStructureView(final FileEditor fileEditor, @NotNull final Project project) {
        final StructureView structureView = super.createStructureView(fileEditor, project);
        if (structureView instanceof StructureViewComponent) {
            ((StructureViewComponent) structureView).getTree().setRootVisible(false);
        }
        return structureView;
    }
}
