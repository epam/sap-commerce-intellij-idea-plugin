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
package com.intellij.idea.plugin.hybris.beans.meta.impl

import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModel
import com.intellij.idea.plugin.hybris.beans.meta.model.*
import com.intellij.idea.plugin.hybris.beans.meta.model.impl.*
import com.intellij.idea.plugin.hybris.beans.model.*
import com.intellij.idea.plugin.hybris.beans.model.Enum
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile

class BeansMetaModelBuilder(
    private val myModule: Module,
    private val myPsiFile: PsiFile,
    private val myCustom: Boolean
) {

    private val myMetaModel = BeansMetaModel(myModule, myPsiFile, myCustom)

    fun build() = myMetaModel

    fun withEnumTypes(types: List<Enum>): BeansMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, BeansMetaType.META_ENUM) }

        return this
    }

    fun withBeanTypes(types: List<Bean>) = withBeanTypes(types, BeanType.BEAN, BeansMetaType.META_BEAN)

    fun withEventTypes(types: List<Bean>) = withBeanTypes(types, BeanType.EVENT, BeansMetaType.META_EVENT)

    private fun withBeanTypes(types: List<Bean>, type: BeanType, targetType: BeansMetaType): BeansMetaModelBuilder {
        types
            .filter { (it.type.value ?: BeanType.BEAN) == type}
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, targetType) }

        return this
    }

    private fun create(dom: Enum): BeansMetaEnum? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaEnumImpl(
            dom, myModule, name, myCustom,
            values = createEnumValues(dom)
        )
    }

    private fun create(dom: Bean): BeansMetaBean? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaBeanImpl(
            dom, myModule, name, myCustom,
            imports = createImports(dom.imports),
            annotations = createAnnotations(dom.annotationses),
            properties = createProperties(dom.properties),
            hints = createHints(dom.hints),
        )
    }

    private fun createHints(dom: Hints): Map<String, BeansMetaHint> = dom.hints
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { hint -> hint.name?.trim { it <= ' ' } }

    private fun createProperties(dom: List<Property>): Map<String, BeansMetaProperty> = dom
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { property -> property.name?.trim { it <= ' ' } }

    private fun createAnnotations(dom: List<Annotations>) = dom
        .map { create(it) }

    private fun createImports(dom: List<Import>) = dom
        .map { create(it) }

    private fun createEnumValues(dom: Enum): Map<String, BeansMetaEnum.BeansMetaEnumValue> = dom.values
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name?.trim { it <= ' ' } }

    private fun create(dom: EnumValue): BeansMetaEnum.BeansMetaEnumValue? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaEnumImpl.BeansMetaEnumValueImpl(dom, myModule, myCustom, name)
    }

    private fun create(dom: Annotations) = BeansMetaAnnotationsImpl(dom, myModule, myCustom, null)

    private fun create(dom: Import) = BeansMetaImportImpl(dom, myModule, myCustom, null)

    private fun create(dom: Property): BeansMetaProperty? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaPropertyImpl(
            dom, myModule, myCustom, name,
            createAnnotations(dom.annotationses),
            createHints(dom.hints)
        )
    }

    private fun create(dom: Hint): BeansMetaHint? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaHintImpl(dom, myModule, myCustom, name)
    }

}