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

package com.intellij.idea.plugin.hybris.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.xml.XmlFileTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Function;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomElementsNavigationManager;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.structure.DomStructureViewTreeModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/2/17.
 */
public class TSStructureViewTreeModel extends DomStructureViewTreeModel
    implements StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {

    private final DomElementNavigationProvider myNavigationProvider;
    private final Function<DomElement, DomService.StructureViewMode> myDescriptor;

    public TSStructureViewTreeModel(
        @NotNull XmlFile file,
        @NotNull Function<DomElement, DomService.StructureViewMode> descriptor,
        @Nullable Editor editor
    ) {
        super(
            file,
            DomElementsNavigationManager.getManager(file.getProject())
                                        .getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME),
            descriptor,
            editor
        );
        myNavigationProvider = DomElementsNavigationManager.getManager(file.getProject())
                                                           .getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME);
        myDescriptor = descriptor;
    }

    @Override
    @NotNull
    public StructureViewTreeElement getRoot() {
        XmlFile myFile = getPsiFile();
        final DomFileElement<DomElement> fileElement = DomManager.getDomManager(myFile.getProject()).getFileElement(
            myFile,
            DomElement.class
        );
        return fileElement == null ?
            new XmlFileTreeElement(myFile) :
            new TSStructureTreeElement(
                fileElement.getRootElement().createStableCopy(),
                myDescriptor,
                myNavigationProvider
            );
    }

    @Override
    public boolean isAlwaysShowsPlus(final StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(final StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAutoExpand(@NotNull final StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isSmartExpand() {
        return true;
    }
}
