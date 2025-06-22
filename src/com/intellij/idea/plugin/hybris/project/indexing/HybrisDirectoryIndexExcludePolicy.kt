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

package com.intellij.idea.plugin.hybris.project.indexing

import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModel
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.pointers.VirtualFilePointer
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager
import com.intellij.util.application

class HybrisDirectoryIndexExcludePolicy(project: Project) : DirectoryIndexExcludePolicy {

    override fun getExcludeRootsForModule(rootModel: ModuleRootModel): Array<VirtualFilePointer> {
        val contentRoots = rootModel.contentRoots
        val excludedFoldersFromIndex = mutableListOf<VirtualFilePointer>()
        contentRoots.forEach { contentRoot ->
            excludedFoldersFromIndex.addAll(getExcludedFoldersFromIndex(contentRoot))
        }
        return excludedFoldersFromIndex.toTypedArray()
    }

    private fun getExcludedFoldersFromIndex(contentRoot: VirtualFile): List<VirtualFilePointer> {
        val excludedFoldersFromIndex = mutableListOf<VirtualFilePointer>()
        getExcludedFromIndexList().forEach { excludedFolderPath ->
            val visitor = HybrisExcludeFromIndexFileVisitor(
                excludedFolderPath, excludedFoldersFromIndex, VirtualFileVisitor.SKIP_ROOT
            )
            VfsUtilCore.visitChildrenRecursively(contentRoot, visitor)
        }
        return excludedFoldersFromIndex
    }

    private fun getExcludedFromIndexList(): List<String> {
        return ApplicationSettingsComponent.getInstance()
            .state
            .excludedFromIndexList
    }

    internal class HybrisExcludeFromIndexFileVisitor(
        excludedFolderPath: String,
        private val excludedFoldersFromIndex: MutableList<VirtualFilePointer>,
        vararg options: Option?,
    ) : VirtualFileVisitor<VirtualFile>(*options) {

        private val virtualFilePointerProvider = HybrisVirtualFilePointerProvider.getInstance()
        private val pathFragments: List<String> = excludedFolderPath.split("/")

        private var currentDepth = 0

        override fun visitFile(file: VirtualFile): Boolean {
            return file.isDirectory && pathFragments.size > currentDepth &&
                isFolderNameEqualToPathFragment(file, currentDepth)
        }

        override fun visitFileEx(file: VirtualFile): Result {
            if (visitFile(file)) {
                currentDepth++
                return CONTINUE
            }
            if (isFolderNameUndefined(currentDepth)) {
                changeContentRoot(file)
            }
            return SKIP_CHILDREN
        }

        private fun changeContentRoot(file: VirtualFile) {
            val excludedFolderPath = pathFragments.drop(pathFragments.indexOf("**") + 1).joinToString("/")
            val fileVisitor = HybrisExcludeFromIndexFileVisitor(excludedFolderPath, excludedFoldersFromIndex, SKIP_ROOT)
            VfsUtilCore.visitChildrenRecursively(file, fileVisitor)
        }

        override fun afterChildrenVisited(file: VirtualFile) {
            if (isFolderNameEqualToPathFragment(file, pathFragments.size - 1)) {
                excludedFoldersFromIndex.add(virtualFilePointerProvider.createVirtualFilePointer(file))
            }
        }

        private fun isFolderNameEqualToPathFragment(file: VirtualFile, fragmentIndex: Int): Boolean {
            return file.name == pathFragments[fragmentIndex]
        }

        private fun isFolderNameUndefined(fragmentIndex: Int): Boolean {
            return pathFragments.size > fragmentIndex && pathFragments[fragmentIndex] == "**"
        }

    }

    @Service
    internal class HybrisVirtualFilePointerProvider() : Disposable {
        private val virtualFilePointerManager = VirtualFilePointerManager.getInstance()

        fun createVirtualFilePointer(vf: VirtualFile): VirtualFilePointer = virtualFilePointerManager.create(vf.url, this, null)

        override fun dispose() = Unit

        companion object {
            fun getInstance(): HybrisVirtualFilePointerProvider = application.getService(HybrisVirtualFilePointerProvider::class.java)
        }
    }
}
