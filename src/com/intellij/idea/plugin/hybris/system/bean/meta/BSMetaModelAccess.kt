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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

interface BSMetaModelAccess {

    companion object {
        val TOPIC = Topic("HYBRIS_BEANS_LISTENER", BSChangeListener::class.java)

        fun getInstance(project: Project): BSMetaModelAccess = project.getService(BSMetaModelAccess::class.java)
    }

    fun getMetaModel(): BSGlobalMetaModel
    fun findMetaEnumByName(name: String?): BSGlobalMetaEnum?
    fun findMetaForDom(dom: Enum): BSGlobalMetaEnum?
    fun findMetasForDom(dom: Bean): List<BSGlobalMetaBean>
    fun findMetaBeansByName(name: String?): List<BSGlobalMetaBean>
    fun findMetasByName(name: String): List<BSGlobalMetaClassifier<*>>
    fun <T : BSGlobalMetaClassifier<*>> getAll(metaType: BSMetaType): Collection<T>

}