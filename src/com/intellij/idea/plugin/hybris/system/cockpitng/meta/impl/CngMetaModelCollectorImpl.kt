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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelCollector
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.EditorDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.WidgetDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.Widgets
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Processor
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.stubs.index.DomElementClassIndex
import java.util.*

class CngMetaModelCollectorImpl(private val myProject: Project) : CngMetaModelCollector {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun <T : DomElement> collectDependencies(clazz: Class<T>, shouldCollect: (DomFileElement<T>) -> Boolean): Set<PsiFile> {
        val files = HashSet<PsiFile>()

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.collectingDependencies", getDescriptor(clazz))

        StubIndex.getInstance().processElements(
            DomElementClassIndex.KEY,
            clazz.name,
            myProject,
            ProjectScope.getAllScope(myProject),
            PsiFile::class.java,
            object : Processor<PsiFile> {
                override fun process(psiFile: PsiFile): Boolean {
                    psiFile.virtualFile ?: return true
                    val domFileElement = myDomManager.getFileElement(psiFile as XmlFile, clazz) ?: return true

                    if (shouldCollect.invoke(domFileElement)) {
                        files.add(psiFile)
                    }
                    return true
                }
            }
        )

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.collectedDependencies", files.size, getDescriptor(clazz))

        return Collections.unmodifiableSet(files)
    }

    private fun <T : DomElement> getDescriptor(clazz: Class<T>) = when (clazz) {
        Config::class.java -> "Configuration"
        ActionDefinition::class.java -> "Action Definition"
        WidgetDefinition::class.java -> "Widget Definition"
        EditorDefinition::class.java -> "Editor Definition"
        Widgets::class.java -> "Widgets"
        else -> "Other"
    }
}