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

import com.intellij.ide.structureView.newStructureView.StructureViewComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Function
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService
import com.intellij.util.xml.structure.DomStructureViewBuilder

class TSStructureViewBuilder(
    private val myFile: XmlFile,
    private val myDescriptor: Function<DomElement, DomService.StructureViewMode>
) : DomStructureViewBuilder(myFile, myDescriptor) {

    override fun createStructureViewModel(editor: Editor?) = TSStructureViewTreeModel(myFile, myDescriptor, editor)

    override fun createStructureView(fileEditor: FileEditor?, project: Project) = with(super.createStructureView(fileEditor, project)) {
        (this as? StructureViewComponent)
            ?.tree
            ?.isRootVisible = false
        this
    }
}