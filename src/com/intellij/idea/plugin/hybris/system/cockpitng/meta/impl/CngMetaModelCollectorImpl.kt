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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.impl

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelCollector
import com.intellij.idea.plugin.hybris.system.cockpitng.model.Config
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelCollector
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Processor
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.stubs.index.DomElementClassIndex
import java.util.*

class CngMetaModelCollectorImpl(private val myProject: Project) : CngMetaModelCollector {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun collectDependencies(): Set<PsiFile> {
        val files = HashSet<PsiFile>()

        try {
            return ProgressManager.getInstance()
                .computeInNonCancelableSection(ThrowableComputable<Set<PsiFile>, Exception> {
                    StubIndex.getInstance().processElements(
                        DomElementClassIndex.KEY,
                        Config::class.java.name,
                        myProject,
                        ProjectScope.getAllScope(myProject),
                        PsiFile::class.java,
                        object : Processor<PsiFile> {
                            override fun process(psiFile: PsiFile): Boolean {
                                psiFile.virtualFile ?: return true
                                myDomManager.getFileElement(psiFile as XmlFile, Config::class.java) ?: return true

                                files.add(psiFile)
                                return true
                            }
                        }
                    )

                    Collections.unmodifiableSet(files)
                })
        } catch (e : Exception) {
            // can happen due broken Stub index, and requested reindex via FileBasedIndexImpl, cancel for now and try again later
            throw ProcessCanceledException(e);
        }
    }
}