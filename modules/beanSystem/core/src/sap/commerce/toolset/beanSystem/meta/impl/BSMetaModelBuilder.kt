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
package sap.commerce.toolset.beanSystem.meta.impl

import sap.commerce.toolset.CaseInsensitiveMap
import sap.commerce.toolset.beanSystem.meta.model.*
import sap.commerce.toolset.beanSystem.meta.model.impl.*
import sap.commerce.toolset.beanSystem.model.*
import sap.commerce.toolset.beanSystem.model.Enum
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.associateByTo
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.text.trim

class BSMetaModelBuilder(
    private val moduleName: String,
    private val extensionName: String,
    fileName: String,
    private val custom: Boolean,
) {

    private val myMetaModel = _root_ide_package_.sap.commerce.toolset.beanSystem.meta.BSMetaModel(extensionName, fileName, custom)

    fun build() = myMetaModel

    fun withEnumTypes(types: List<Enum>): BSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, BSMetaType.META_ENUM) }

        return this
    }

    fun withBeanTypes(types: List<Bean>) = withBeanTypes(types, BeanType.BEAN, BSMetaType.META_BEAN)

    fun withEventTypes(types: List<Bean>) = withBeanTypes(types, BeanType.EVENT, BSMetaType.META_EVENT)

    private fun withBeanTypes(types: List<Bean>, type: BeanType, targetType: BSMetaType): BSMetaModelBuilder {
        types
            .filter { (it.type.value ?: BeanType.BEAN) == type }
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, targetType) }

        return this
    }

    private fun create(dom: Enum): BSMetaEnum? {
        val name = BSMetaModelNameProvider.extract(dom) ?: return null
        return BSMetaEnumImpl(
            dom, moduleName, extensionName, name, custom,
            values = createEnumValues(dom)
        )
    }

    private fun create(dom: Bean): BSMetaBean? {
        val name = BSMetaModelNameProvider.extract(dom) ?: return null
        return BSMetaBeanImpl(
            dom, moduleName, extensionName, name, custom,
            imports = createImports(dom.imports),
            annotations = createAnnotations(dom.annotationses),
            properties = createProperties(dom.properties),
            hints = createHints(dom.hints),
        )
    }

    private fun createHints(dom: Hints): Map<String, BSMetaHint> = dom.hints
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitiveMap.CaseInsensitiveConcurrentHashMap()) { hint -> hint.name?.trim { it <= ' ' } }

    private fun createProperties(dom: List<Property>): Map<String, BSMetaProperty> = dom
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitiveMap.CaseInsensitiveConcurrentHashMap()) { property -> property.name?.trim { it <= ' ' } }

    private fun createAnnotations(dom: List<Annotations>) = dom
        .map { create(it) }

    private fun createImports(dom: List<Import>) = dom
        .map { create(it) }

    private fun createEnumValues(dom: Enum): Map<String, BSMetaEnum.BSMetaEnumValue> = dom.values
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitiveMap.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name?.trim { it <= ' ' } }

    private fun create(dom: EnumValue): BSMetaEnum.BSMetaEnumValue? {
        val name = BSMetaModelNameProvider.extract(dom) ?: return null
        return BSMetaEnumImpl.BSMetaEnumValueImpl(dom, moduleName, extensionName, custom, name)
    }

    private fun create(dom: Annotations) = BSMetaAnnotationsImpl(dom, moduleName, extensionName, custom, null)

    private fun create(dom: Import) = BSMetaImportImpl(dom, moduleName, extensionName, custom)

    private fun create(dom: Property): BSMetaProperty? {
        val name = BSMetaModelNameProvider.extract(dom) ?: return null
        return BSMetaPropertyImpl(
            dom, moduleName, extensionName, custom, name,
            createAnnotations(dom.annotationses),
            createHints(dom.hints)
        )
    }

    private fun create(dom: Hint): BSMetaHint? {
        val name = BSMetaModelNameProvider.extract(dom) ?: return null
        return BSMetaHintImpl(dom, moduleName, extensionName, custom, name)
    }

}