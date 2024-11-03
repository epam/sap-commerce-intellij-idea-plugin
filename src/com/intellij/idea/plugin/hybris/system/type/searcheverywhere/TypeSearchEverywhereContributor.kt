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
package com.intellij.idea.plugin.hybris.system.type.searcheverywhere

import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.ide.util.gotoByName.LanguageRef
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import javax.swing.ListCellRenderer

class TypeSearchEverywhereContributor(event: AnActionEvent) : AbstractGotoSEContributor(event), SearchEverywherePreviewProvider {
//    private val filter = createLanguageFilter(event.getRequiredData(CommonDataKeys.PROJECT))

    override fun createModel(project: Project): FilteringGotoByModel<LanguageRef> {
        val model = GotoTypeModel(project, listOf(TypeChooseByNameContributor()))
//        model.setFilterItems(filter.selectedElements)
        return model
    }

    override fun isShownInSeparateTab() = true
    override fun getGroupName() = "SAP Types"
    override fun getFullGroupName() = "SAP Types/"
    override fun getSortWeight() = 2000

    override fun getElementsRenderer(): ListCellRenderer<in Any?> {
        return object : SearchEverywherePsiRenderer(this) {

            override fun getIcon(element: PsiElement?) = when (element?.parentOfType<XmlTag>()?.localName) {
                ItemTypes.ITEMTYPE -> HybrisIcons.TypeSystem.Types.ITEM
                CollectionTypes.COLLECTIONTYPE -> HybrisIcons.TypeSystem.Types.COLLECTION
                EnumTypes.ENUMTYPE -> HybrisIcons.TypeSystem.Types.ENUM
                Relations.RELATION -> HybrisIcons.TypeSystem.Types.RELATION
                MapTypes.MAPTYPE -> HybrisIcons.TypeSystem.Types.MAP
                else -> null
            }

            override fun getElementText(element: PsiElement?) = element
                ?.text
                ?.let { StringUtil.unquoteString(it) }
        }
    }

    class Factory : SearchEverywhereContributorFactory<Any?> {
        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any?> {
            return PSIPresentationBgRendererWrapper.wrapIfNecessary(TypeSearchEverywhereContributor(initEvent))
        }
    }

    companion object {
        // TODO: introduce custom "LanguageRef"
//        @JvmStatic
//        fun createLanguageFilter(project: Project): PersistentSearchEverywhereContributorFilter<LanguageRef> {
//            val items = forAllLanguages()
//            val persistentConfig = GotoTypeConfiguration.getInstance(project)
//            return PersistentSearchEverywhereContributorFilter(items, persistentConfig, LanguageRef::displayName, LanguageRef::icon)
//        }
    }

    class TypeChooseByNameContributor : ChooseByNameContributor {
        override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
            if (project == null) return emptyArray()
            return TSMetaModelAccess.getInstance(project).getAll()
                .mapNotNull { it.name }
                .toTypedArray()
        }

        override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
            if (project == null) return emptyArray()
            if (name == null) return emptyArray()
            if (pattern == null) return emptyArray()

            return TSMetaModelAccess.getInstance(project).getAll()
                .filter { it.name?.lowercase()?.contains(pattern.lowercase()) ?: false }
                .flatMap { it.retrieveAllDoms() }
                .mapNotNull {
                    when (it) {
                        is CollectionType -> it.code.xmlAttributeValue
                        is EnumType -> it.code.xmlAttributeValue
                        is MapType -> it.code.xmlAttributeValue
                        is Relation -> it.code.xmlAttributeValue
                        is ItemType -> it.code.xmlAttributeValue
                        else -> null
                    }
                }
                .mapNotNull { it as? NavigationItem }
                .toTypedArray()
        }
    }
}
