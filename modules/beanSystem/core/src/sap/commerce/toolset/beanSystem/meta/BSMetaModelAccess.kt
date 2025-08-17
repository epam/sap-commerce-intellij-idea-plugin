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
package sap.commerce.toolset.beanSystem.meta

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean
import sap.commerce.toolset.beanSystem.model.Bean
import sap.commerce.toolset.beanSystem.model.Enum

@Service(Service.Level.PROJECT)
class BSMetaModelAccess(private val project: Project) {

    fun getAllBeans() = getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean>(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_BEAN) +
        getAll(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_WS_BEAN) +
        getAll(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_EVENT)

    fun getAllEnums() = getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaEnum>(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_ENUM)

    fun <T : sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier<*>> getAll(metaType: sap.commerce.toolset.beanSystem.meta.model.BSMetaType): Collection<T> = BSMetaModelStateService.state(project).getMetaType<T>(metaType).values

    fun findMetaForDom(dom: Enum) = findMetaEnumByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.impl.BSMetaModelNameProvider.extract(dom))
    fun findMetasForDom(dom: Bean): List<BSGlobalMetaBean> = _root_ide_package_.sap.commerce.toolset.beanSystem.meta.impl.BSMetaModelNameProvider.extract(dom)
        ?.let { findMetaBeansByName(it) }
        ?: emptyList()

    fun findMetaBeanByName(name: String?) = listOfNotNull(
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_WS_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_EVENT, name)
    )
        .map { it as? BSGlobalMetaBean }
        .firstOrNull()

    fun findMetaBeansByName(name: String?): List<BSGlobalMetaBean> = listOfNotNull(
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_WS_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_EVENT, name)
    )

    fun findMetasByName(name: String): List<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier<*>> = listOfNotNull(
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_ENUM, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_WS_BEAN, name),
        findMetaByName(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_EVENT, name)
    )

    fun findMetaEnumByName(name: String?) = findMetaByName<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaEnum>(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_ENUM, name)

    private fun <T : sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier<*>> findMetaByName(metaType: sap.commerce.toolset.beanSystem.meta.model.BSMetaType, name: String?): T? = BSMetaModelStateService.state(project)
        .getMetaType<T>(metaType)[name]

    companion object {
        fun getInstance(project: Project): BSMetaModelAccess = project.service()
    }

}