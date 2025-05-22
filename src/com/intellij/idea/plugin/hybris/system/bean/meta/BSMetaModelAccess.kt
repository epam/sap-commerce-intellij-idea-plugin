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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class BSMetaModelAccess(project: Project) {

    companion object {
        fun getInstance(project: Project): BSMetaModelAccess = project.getService(BSMetaModelAccess::class.java)
    }

    private val metaModelStateService = project.service<BSMetaModelStateService>()

    fun getAllBeans() = getAll<BSGlobalMetaBean>(BSMetaType.META_BEAN) +
        getAll(BSMetaType.META_WS_BEAN) +
        getAll(BSMetaType.META_EVENT)

    fun getAllEnums() = getAll<BSGlobalMetaEnum>(BSMetaType.META_ENUM)

    fun <T : BSGlobalMetaClassifier<*>> getAll(metaType: BSMetaType): Collection<T> = metaModelStateService.get().getMetaType<T>(metaType).values

    fun findMetaForDom(dom: Enum) = findMetaEnumByName(BSMetaModelNameProvider.extract(dom))
    fun findMetasForDom(dom: Bean): List<BSGlobalMetaBean> = BSMetaModelNameProvider.extract(dom)
        ?.let { findMetaBeansByName(it) }
        ?: emptyList()

    fun findMetaBeanByName(name: String?) = listOfNotNull(
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )
        .map { it as? BSGlobalMetaBean }
        .firstOrNull()

    fun findMetaBeansByName(name: String?): List<BSGlobalMetaBean> = listOfNotNull(
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )

    fun findMetasByName(name: String): List<BSGlobalMetaClassifier<*>> = listOfNotNull(
        findMetaByName(BSMetaType.META_ENUM, name),
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )

    fun findMetaEnumByName(name: String?) = findMetaByName<BSGlobalMetaEnum>(BSMetaType.META_ENUM, name)

    private fun <T : BSGlobalMetaClassifier<*>> findMetaByName(metaType: BSMetaType, name: String?): T? = metaModelStateService.get()
        .getMetaType<T>(metaType)[name]

}