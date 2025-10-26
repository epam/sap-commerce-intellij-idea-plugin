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

package sap.commerce.toolset.meta

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Processor
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.stubs.index.DomElementClassIndex
import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.project.yExtensionName

abstract class MetaCollector<T : DomElement>(
    protected val project: Project,
    private val clazz: Class<T>,
    private val takeIf: (T) -> Boolean = { true },
    private val nameProvider: (VirtualFile) -> String,
    private val representationNameProvider: (VirtualFile, T) -> String = { vf, _ -> vf.name },
) {

    open suspend fun collectDependencies(): Set<Meta<T>> {
        val myDomManager: DomManager = DomManager.getDomManager(project)
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project)
        val files = HashSet<Meta<T>>()

        smartReadAction(project) {
            StubIndex.getInstance().processElements(
                DomElementClassIndex.KEY,
                clazz.name,
                project,
                ProjectScope.getAllScope(project),
                PsiFile::class.java,
                object : Processor<PsiFile> {
                    override fun process(psiFile: PsiFile): Boolean {
                        val xmlFile = psiFile.asSafely<XmlFile>() ?: return true
                        val virtualFile = xmlFile.virtualFile ?: return true
                        val metaContainer = projectFileIndex.getModuleForFile(virtualFile)
                            ?.let { it.name to it.yExtensionName() }
                        // Some files are part of the Library and, as a result, aren't associated with any Module
                            ?: libraryTable.libraries
                                .firstNotNullOfOrNull { library ->
                                    library.getFiles(OrderRootType.CLASSES)
                                        .firstOrNull { libraryVirtualFile -> VfsUtilCore.isAncestor(libraryVirtualFile, virtualFile, false) }
                                        ?.let { library.presentableName to it.name }
                                }
                            ?: return true
                        val rootElement = myDomManager.getFileElement(psiFile, clazz)
                            ?.rootElement
                            ?.takeIf(takeIf)
                            ?: return true

                        val meta = Meta(
                            metaContainer.first, metaContainer.second, psiFile, virtualFile, rootElement,
                            nameProvider.invoke(virtualFile),
                            representationNameProvider.invoke(virtualFile, rootElement),
                        )
                        files.add(meta)

                        return true
                    }
                }
            )
        }

        return files.toImmutableSet()
    }
}