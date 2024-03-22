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

package com.intellij.idea.plugin.hybris.system.spring

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.concurrency.AppExecutorUtil

/**
 * Incredibly simple handling of the Spring beans.
 * Provided only as a fallback logic for IntelliJ Community.
 * It is not planned for future improvement or IntelliJ IDEA Ultimate Spring plugin replacement.
 * May produce incorrect results.
 */
@Service(Service.Level.PROJECT)
class SimpleSpringService(val project: Project) {

    private val cache = CachedValuesManager.getManager(project).createCachedValue(
        {
            val springFiles = collectFiles()
            val beans = processBeans(springFiles)

            val dependencies = springFiles
                .toTypedArray()

            CachedValueProvider.Result.create(beans, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    fun findBean(id: String) = if (cache.hasUpToDateValue()) cache.value[id]
    else null

    fun initCache() {
        ReadAction
            .nonBlocking<Map<String, XmlTag>>() {
                cache.value
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private fun processBeans(xmlFiles: List<XmlFile>) = xmlFiles
        .mapNotNull { it.rootTag }
        .flatMap {
            it.childrenOfType<XmlTag>()
                .filter { tag -> tag.localName == "bean" }
                .mapNotNull { tag ->
                    val id = tag.getAttributeValue("id") ?: return@mapNotNull null
                    tag.getAttributeValue("class") ?: return@mapNotNull null

                    id to tag
                }
        }
        .associate { it.first to it.second }

    private fun collectFiles(): List<XmlFile> {
        val psiManager = PsiManager.getInstance(project)

        return FileTypeIndex.getFiles(
            XmlFileType.INSTANCE,
            GlobalSearchScope.allScope(project)
        )
            .mapNotNull { psiManager.findFile(it) }
            .filterIsInstance<XmlFile>()
            .filter { it.rootTag?.getAttributeValue("xmlns") == HybrisConstants.SPRING_NAMESPACE }
    }

    companion object {
        fun getService(project: Project) = if (HybrisConstants.IDEA_EDITION_ULTIMATE.equals(ApplicationNamesInfo.getInstance().editionName, ignoreCase = true)) null
        else project.getService(SimpleSpringService::class.java)
    }
}