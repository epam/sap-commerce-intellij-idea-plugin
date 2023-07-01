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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xml.DomElement

interface TSMetaModelAccess {

    companion object {
        val TOPIC = Topic("HYBRIS_TYPE_SYSTEM_LISTENER", TSChangeListener::class.java)
        fun getInstance(project: Project): TSMetaModelAccess = project.getService(TSMetaModelAccess::class.java)
    }

    fun initMetaModel()
    fun getMetaModel(): TSGlobalMetaModel
    fun findMetaClassifierByName(name: String?): TSGlobalMetaClassifier<out DomElement>?
    fun findRelationByName(name: String?): List<TSMetaRelation>
    fun findMetaMapByName(name: String?): TSGlobalMetaMap?
    fun findMetaCollectionByName(name: String?): TSGlobalMetaCollection?
    fun findMetaAtomicByName(name: String?): TSGlobalMetaAtomic?
    fun findMetaEnumByName(name: String?): TSGlobalMetaEnum?
    fun findMetaItemByName(name: String?): TSGlobalMetaItem?
    fun findMetaRelationByName(name: String?): TSGlobalMetaRelation?
    fun findMetaForDom(dom: ItemType): TSGlobalMetaItem?
    fun findMetaForDom(dom: EnumType): TSGlobalMetaEnum?
    fun <T : TSGlobalMetaClassifier<*>> getAll(metaType: TSMetaType): Collection<T>
    fun getAll(): Collection<TSGlobalMetaClassifier<*>>
    fun getNextAvailableTypeCode(): Int
}