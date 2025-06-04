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
package com.intellij.idea.plugin.hybris.system.type.searcheverywhere

import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
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

    private val filter = createSystemFilter(project)

    override fun createModel(project: Project): FilteringGotoByModel<SystemRef> {
        val model = GotoTypeModel(project, listOf(TypeChooseByNameContributor()))

        model.setFilterItems(filter.selectedElements)
        return model
    }

    override fun isShownInSeparateTab() = true
    override fun getGroupName() = "[y] Types"
    override fun getFullGroupName() = "[y] Types/"
    override fun getSortWeight() = 2000
    override fun getActions(onChanged: Runnable) = doGetActions(filter, null, onChanged)

    override fun getElementsRenderer(): ListCellRenderer<in Any?> = object : SearchEverywherePsiRenderer(this) {
        override fun getIcon(element: PsiElement?) = when (element?.parentOfType<XmlTag>()?.localName) {
            ItemTypes.ITEMTYPE -> HybrisIcons.TypeSystem.Types.ITEM
            CollectionTypes.COLLECTIONTYPE -> HybrisIcons.TypeSystem.Types.COLLECTION
            EnumTypes.ENUMTYPE -> HybrisIcons.TypeSystem.Types.ENUM
            Relations.RELATION -> HybrisIcons.TypeSystem.Types.RELATION
            MapTypes.MAPTYPE -> HybrisIcons.TypeSystem.Types.MAP

            Beans.BEAN -> HybrisIcons.BeanSystem.BEAN
            Beans.ENUM -> HybrisIcons.BeanSystem.ENUM

            else -> null
        }

        override fun getElementText(element: PsiElement?) = element
            ?.text
            ?.let { StringUtil.unquoteString(it) }
    }

    class Factory : SearchEverywhereContributorFactory<Any?> {
        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any?> {
            return PSIPresentationBgRendererWrapper.wrapIfNecessary(TypeSearchEverywhereContributor(initEvent))
        }
    }

    companion object {
        fun createSystemFilter(project: Project) = PersistentSearchEverywhereContributorFilter(
            SystemRef.forAllSystems(),
            GotoTypeConfiguration.getInstance(project),
            SystemRef::displayName,
            SystemRef::icon
        )
    }

    private class TypeChooseByNameContributor : ChooseByNameContributor {
        override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
            if (project == null) return emptyArray()

            return buildList {
                addAll(TSMetaModelAccess.getInstance(project).getAll().mapNotNull { it.name })
                addAll(BSMetaModelAccess.getInstance(project).getAllBeans().mapNotNull { it.name })
                addAll(BSMetaModelAccess.getInstance(project).getAllEnums().mapNotNull { it.name })
            }
                .toTypedArray()
        }

        override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
            if (project == null || name == null || pattern == null) return emptyArray()

            val types = TSMetaModelAccess.getInstance(project).getAll()
                .filter { it.name?.lowercase()?.contains(pattern.lowercase()) == true }
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

            val beans = BSMetaModelAccess.getInstance(project).getAllBeans()
                .filter { it.name?.lowercase()?.contains(pattern.lowercase()) == true }
                .flatMap { it.retrieveAllDoms() }
                .mapNotNull { it.clazz.xmlAttributeValue }

            val enums = BSMetaModelAccess.getInstance(project).getAllEnums()
                .filter { it.name?.lowercase()?.contains(pattern.lowercase()) == true }
                .flatMap { it.retrieveAllDoms() }
                .mapNotNull { it.clazz.xmlAttributeValue }

            return (types + beans + enums)
                .mapNotNull { it as? NavigationItem }
                .toTypedArray()
        }
    }
}
