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
package com.intellij.idea.plugin.hybris.type.system.meta

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation.TSMetaRelationElement
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import java.util.*

interface TSMetaModelAccess {

    companion object {
        fun getInstance(project: Project): TSMetaModelAccess = project.getService(TSMetaModelAccess::class.java)
    }

    fun getMetaModel(): TSGlobalMetaModel
    fun collectReferencesForSourceType(source: TSMetaItem, out: LinkedList<TSMetaRelationElement?>)
    fun findMetaClassifierByName(name: String?): TSMetaClassifier<out DomElement>?
    fun findRelationByName(name: String?): List<TSMetaRelation>
    fun findMetaMapByName(name: String?): TSMetaMap?
    fun findMetaCollectionByName(name: String?): TSMetaCollection?
    fun findMetaAtomicByName(name: String?): TSMetaAtomic?
    fun findMetaEnumByName(name: String?): TSMetaEnum?
    fun findMetaItemByName(name: String?): TSMetaItem?
    fun findMetaItemForDom(dom: ItemType): TSMetaItem?
    fun <T : TSMetaClassifier<out DomElement>?> getAll(metaType: MetaType): Collection<T>

}