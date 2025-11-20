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

package sap.commerce.toolset.spring

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.HybrisConstants
import java.io.Serial

/**
 * Incredibly simple handling of the Spring beans.
 * Provided only as a fallback logic for IntelliJ Community.
 * It is not planned for future improvement or IntelliJ IDEA Ultimate Spring plugin replacement.
 * May produce incorrect results.
 */
@Service(Service.Level.PROJECT)
class SimpleSpringService(private val project: Project, private val coroutineScope: CoroutineScope) : SpringService {

    private val computing = AtomicBooleanProperty(false)

    private val cache = CachedValuesManager.getManager(project).createCachedValue(
        {
            val springFiles = collectFiles()
            // id -> bean
            val beans = processBeans(springFiles)
            // alias -> name
            val aliases = processAliases(springFiles)

            val mutableBeans = beans.toMutableMap()
            aliases.forEach { (alias, name) ->
                val bean = mutableBeans[name] ?: return@forEach
                mutableBeans[alias] = bean
            }

            val dependencies = springFiles
                .mapNotNull { it.virtualFile }
                .toTypedArray()

            CachedValueProvider.Result.create(mutableBeans.toImmutableMap(), dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    fun initCache() = coroutineScope.launch {
        computing.set(true)

        withBackgroundProgress(project, "Init simple Spring context", true) {
            smartReadAction(project) { cache.value }
        }

        computing.set(false)
    }

    override fun resolveBeanDeclaration(element: PsiElement, beanId: String) = findBean(beanId)

    override fun resolveBeanClass(element: PsiElement, beanId: String) = findBean(beanId)
        ?.getAttributeValue("class")
        ?.let {
            JavaPsiFacade.getInstance(element.project).findClass(it, GlobalSearchScope.allScope(element.project))
        }

    private fun findBean(id: String) = cache.value[id]

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

    private fun processAliases(xmlFiles: List<XmlFile>) = xmlFiles
        .mapNotNull { it.rootTag }
        .flatMap {
            it.childrenOfType<XmlTag>()
                .filter { tag -> tag.localName == "alias" }
                .mapNotNull { tag ->
                    val alias = tag.getAttributeValue("alias") ?: return@mapNotNull null
                    val name = tag.getAttributeValue("name") ?: return@mapNotNull null

                    alias to name
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
        @Serial
        private const val serialVersionUID: Long = -8015348108936115374L

        fun getService(project: Project): SimpleSpringService = project.getService(SimpleSpringService::class.java)
    }
}
