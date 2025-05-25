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

package com.intellij.idea.plugin.hybris.system.meta

import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
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

data class Meta<T : DomElement>(
    val moduleName: String,
    val extensionName: String,
    val psiFile: PsiFile,
    val virtualFile: VirtualFile,
    val rootElement: T,
    val name: String,
)

abstract class MetaCollector<T : DomElement>(
    protected val project: Project,
    private val clazz: Class<T>,
    private val takeIf: (T) -> Boolean = { true },
    private val nameProvider: (VirtualFile) -> String,
) {

    private val myDomManager: DomManager = DomManager.getDomManager(project)
    private val projectFileIndex = ProjectFileIndex.getInstance(project)

    open fun collectDependencies(): Set<Meta<T>> {
        val files = HashSet<Meta<T>>()

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
                    val module = projectFileIndex.getModuleForFile(virtualFile) ?: return true
                    val rootElement = myDomManager.getFileElement(psiFile, clazz)
                        ?.rootElement
                        ?.takeIf(takeIf)
                        ?: return true

                    files.add(Meta(module.name, module.yExtensionName(), psiFile, virtualFile, rootElement, nameProvider.invoke(virtualFile)))

                    return true
                }
            }
        )

        return files.toImmutableSet()
    }
}