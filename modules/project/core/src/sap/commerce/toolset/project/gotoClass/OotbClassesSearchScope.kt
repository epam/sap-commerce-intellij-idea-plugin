/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.gotoClass

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants

class OotbClassesSearchScope(project: Project) : GlobalSearchScope(project) {

    override fun isSearchInModuleContent(module: Module) = false

    override fun isSearchInLibraries() = true

    override fun contains(file: VirtualFile): Boolean {
        var virtualFile: VirtualFile? = file

        while (isNotClassesOrDirectories(virtualFile)) {
            virtualFile = virtualFile?.parent
        }
        if (virtualFile == null) return false

        if (virtualFile.name == ProjectConstants.Directory.CLASSES) {
            return virtualFile.parent
                ?.let { virtualFile.findChild(EiConstants.EXTENSION_INFO_XML) != null }
                ?: false
        }

        return JarFileSystem.getInstance().getVirtualFileForJar(file)
            ?.parent
            ?.path
            ?.endsWith(ProjectConstants.Directory.BOOTSTRAP + '/' + ProjectConstants.Directory.BIN)
            ?: false
    }

    private fun isNotClassesOrDirectories(f: VirtualFile?) = f != null
        && !(f.isDirectory && (isClassesOrModels(f)))

    private fun isClassesOrModels(f: VirtualFile) = f.name == ProjectConstants.Directory.CLASSES
        || f.name == HybrisConstants.JAR_MODELS
}
