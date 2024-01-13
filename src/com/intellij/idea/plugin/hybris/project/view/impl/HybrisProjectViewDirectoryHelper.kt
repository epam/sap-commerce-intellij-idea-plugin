/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.view.impl

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.JavaProjectViewDirectoryHelper
import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper
import com.intellij.ide.projectView.impl.nodes.PsiFileSystemItemFilter
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory

class HybrisProjectViewDirectoryHelper(project: Project) : ProjectViewDirectoryHelper(project) {

    private val javaProjectViewDirectoryHelper = JavaProjectViewDirectoryHelper(project)
    private val fileIndex = ProjectFileIndex.getInstance(project)

    override fun getLocationString(psiDirectory: PsiDirectory, includeUrl: Boolean, includeRootType: Boolean) = javaProjectViewDirectoryHelper
        .getLocationString(psiDirectory, includeUrl, includeRootType)

    override fun isShowFQName(settings: ViewSettings?, parentValue: Any?, value: PsiDirectory?) = javaProjectViewDirectoryHelper
        .isShowFQName(settings, parentValue, value)

    override fun shouldHideProjectConfigurationFilesDirectory() = javaProjectViewDirectoryHelper
        .shouldHideProjectConfigurationFilesDirectory()

    override fun getNodeName(settings: ViewSettings?, parentValue: Any?, directory: PsiDirectory?) = javaProjectViewDirectoryHelper
        .getNodeName(settings, parentValue, directory)

    override fun skipDirectory(directory: PsiDirectory?) = javaProjectViewDirectoryHelper
        .skipDirectory(directory)

    override fun canRepresent(element: VirtualFile, directory: PsiDirectory, owner: PsiDirectory, settings: ViewSettings?) = javaProjectViewDirectoryHelper
        .canRepresent(element, directory, owner, settings)

    override fun isEmptyMiddleDirectory(directory: PsiDirectory?, strictlyEmpty: Boolean, filter: PsiFileSystemItemFilter?) = javaProjectViewDirectoryHelper
        .isEmptyMiddleDirectory(directory, strictlyEmpty, filter)

    override fun isEmptyMiddleDirectory(directory: PsiDirectory?, strictlyEmpty: Boolean) = javaProjectViewDirectoryHelper
        .isEmptyMiddleDirectory(directory, strictlyEmpty)

    override fun supportsFlattenPackages() = javaProjectViewDirectoryHelper
        .supportsFlattenPackages()

    override fun supportsHideEmptyMiddlePackages() = javaProjectViewDirectoryHelper
        .supportsHideEmptyMiddlePackages()

    override fun canRepresent(element: Any?, directory: PsiDirectory?) = javaProjectViewDirectoryHelper
        .canRepresent(element, directory)

    override fun getTopLevelRoots(): MutableList<VirtualFile> {
        val topLevelContentRoots = super.getTopLevelRoots()
        if (HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) {
            val prm = ProjectRootManager.getInstance(project)

            for (root in prm.contentRoots) {
                val parent = root.parent
                if (!isFileUnderContentRoot(parent)) {
                    topLevelContentRoots.add(root)
                }
            }
        }

        return topLevelContentRoots
    }

    private fun isFileUnderContentRoot(file: VirtualFile?): Boolean {
        if (file == null) return false
        if (!file.isValid) return false

        val contentRoot = fileIndex.getContentRootForFile(file, false)
            ?: return false

        return !contentRoot.name.endsWith(HybrisConstants.CCV2_CORE_CUSTOMIZE_NAME)
    }
}
