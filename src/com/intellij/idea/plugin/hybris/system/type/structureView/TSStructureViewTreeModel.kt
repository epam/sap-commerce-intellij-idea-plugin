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
package com.intellij.idea.plugin.hybris.system.type.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.xml.XmlFileTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Function
import com.intellij.util.xml.*
import com.intellij.util.xml.structure.DomStructureViewTreeModel

class TSStructureViewTreeModel(
    file: XmlFile,
    private val myDescriptor: Function<DomElement, DomService.StructureViewMode>,
    editor: Editor?
) : DomStructureViewTreeModel(
    file,
    DomElementsNavigationManager.getManager(file.project)
        .getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME),
    myDescriptor,
    editor
), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {

    private val myNavigationProvider: DomElementNavigationProvider

    init {
        myNavigationProvider = DomElementsNavigationManager.getManager(file.project)
            .getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME)
    }

    override fun getRoot(): StructureViewTreeElement {
        val myFile = psiFile
        val fileElement = DomManager.getDomManager(myFile.project).getFileElement(
            myFile,
            DomElement::class.java
        )
        return if (fileElement == null) XmlFileTreeElement(myFile) else TSStructureTreeElement(
            fileElement.rootElement.createStableCopy(),
            myDescriptor,
            myNavigationProvider
        )
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = false

    override fun isSmartExpand() = true
}