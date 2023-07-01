/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.system.type.validation.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.validation.ItemsXmlFileValidation
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import org.apache.commons.collections4.map.CaseInsensitiveMap

class ItemsFileValidation(private val project: Project) : ItemsXmlFileValidation {

    override fun isFileOutOfDate(file: VirtualFile): Boolean {
        if (!HybrisApplicationSettingsComponent.getInstance().state.warnIfGeneratedItemsAreOutOfDate) return false
        if (!file.name.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING)) return false
        if (!ModuleUtil.projectContainsFile(project, file, false)) return false
        if (DumbService.isDumb(project)) return false

        return isFileOutOfDateWithGeneratedClasses(file)
    }

    override fun showNotification() {
        Notifications.create(
            NotificationType.WARNING,
            message("hybris.notification.ts.validation.title"),
            message("hybris.notification.ts.validation.content")
        )
            .hideAfter(10)
            .notify(project)
    }

    private fun isFileOutOfDateWithGeneratedClasses(file: VirtualFile): Boolean {
        try {
            val enumTypeClassValidation = EnumTypeClassValidation.instance
            val itemTypeClassValidation = ItemTypeClassValidation.instance
            val relationValidation = RelationValidation.instance
            val indicator = ProgressManager.getInstance().progressIndicator
            if (indicator != null) {
                indicator.text2 = message(
                    "hybris.startupActivity.itemsXmlValidation.progress.subTitle.validating",
                    file.name
                )
            }
            val psiFile = PsiManager.getInstance(project).findFile(file) as? XmlFile
                ?: return false

            val fileElement = DomManager.getDomManager(project).getFileElement(psiFile, Items::class.java)
                ?: return true

            val items = fileElement.rootElement

            val inheritedEnumClasses = findAllInheritClasses(project, HybrisConstants.CLASS_ENUM_ROOT)
            val enumTypes = items.enumTypes.enumTypes
            if (enumTypeClassValidation.validate(project, enumTypes, inheritedEnumClasses)) return true

            val inheritedItemClasses = findAllInheritClasses(project, HybrisConstants.CLASS_ITEM_ROOT)
            val filteredItemTypes = getItemTypesExcludeRelations(items)
            if (itemTypeClassValidation.validate(project, filteredItemTypes, inheritedItemClasses)) return true

            items.itemTypes.typeGroups
                .find { itemTypeClassValidation.validate(project, it.itemTypes, inheritedItemClasses) }
                ?.let { return true }

            val relations = items.relations.relations
            if (relationValidation.validate(project, relations, inheritedItemClasses)) return true
        } catch (ignore: IndexNotReadyException) {
            //do not validate Items.xml until index is not ready
        } catch (e: Exception) {
            LOG.error(String.format("Items validation error. File: %s", file.name), e)
        }
        return false
    }

    // Some people define relations as items and add attributes to them, we need to exclude those because
    // classes for them often are not being generated at all
    private fun getItemTypesExcludeRelations(itemsRootElement: Items): List<ItemType> {
        val relations = itemsRootElement.relations.relations
            .mapNotNull { it.code.stringValue }
        return itemsRootElement.itemTypes.itemTypes
            .filterNot { relations.contains(it.code.stringValue) }
    }

    private fun findAllInheritClasses(
        project: Project,
        rootClass: String
    ): Map<String, PsiClass> {
        val itemRootClass = JavaPsiFacade.getInstance(project).findClass(
            rootClass, GlobalSearchScope.allScope(project)
        ) ?: return emptyMap()

        if (itemRootClass.name == null) return emptyMap()

        val foundClasses = ClassInheritorsSearch.search(itemRootClass).findAll()
        val result: MutableMap<String, PsiClass> = CaseInsensitiveMap()
        result[itemRootClass.name!!] = itemRootClass
        for (psiClass in foundClasses) {
            psiClass.name
                ?.let { result[it] = psiClass }
        }
        return result
    }

    companion object {
        private val LOG = Logger.getInstance(ItemsFileValidation::class.java)
    }
}
