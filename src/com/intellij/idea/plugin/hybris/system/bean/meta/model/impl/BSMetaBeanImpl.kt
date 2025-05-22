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
package com.intellij.idea.plugin.hybris.system.bean.meta.model.impl

import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.BeanType
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BSMetaBeanImpl(
    dom: Bean,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String,
    override val isCustom: Boolean,
    override val imports: List<BSMetaImport>,
    override val annotations: List<BSMetaAnnotations>,
    override val properties: Map<String, BSMetaProperty>,
    override val hints: Map<String, BSMetaHint>,
) : BSMetaBean {

    override val domAnchor: DomAnchor<Bean> = DomService.getInstance().createAnchor(dom)
    override val description = dom.description.stringValue
    override val template = dom.template.stringValue
    override val genericName = BSMetaHelper.getGenericName(dom.clazz.stringValue)
    override val extends = dom.extends.stringValue
        ?.let { BSMetaHelper.getBeanName(it) }
    override val extendsGenericName = BSMetaHelper.getGenericName(dom.extends.stringValue)
    override val type = dom.type.value ?: BeanType.BEAN
    override val shortName = BSMetaHelper.getShortName(name)
    override val fullName = BSMetaHelper.getNameWithGeneric(name, genericName)
    override val fullExtends = BSMetaHelper.getNameWithGeneric(extends, extendsGenericName)
    override val deprecatedSince = dom.deprecatedSince.stringValue
    override val isDeprecated = dom.deprecated.toBoolean()
    override val isAbstract = dom.abstract.toBoolean()
    override val isSuperEquals = dom.superEquals.toBoolean()

    override fun toString() = "Bean(module=$extensionName, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"

}

internal class BSGlobalMetaBeanImpl(localMeta: BSMetaBean) : BSGlobalMetaBeanSelfMerge<Bean, BSMetaBean>(localMeta), BSGlobalMetaBean {

    override val hints = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BSMetaHint>()
    override val properties = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BSMetaProperty>()
    override val domAnchor = localMeta.domAnchor
    override val type = localMeta.type
    override val shortName = localMeta.shortName
    override var genericName = localMeta.genericName
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override var template = localMeta.template
    override var description = localMeta.description
    override var deprecatedSince = localMeta.deprecatedSince
    override var extends = localMeta.extends
    override var extendsGenericName = localMeta.extendsGenericName
    override var fullName = localMeta.fullName
    override var fullExtends = localMeta.fullExtends
    override val imports = ArrayList<BSMetaImport>()
    override val annotations = ArrayList<BSMetaAnnotations>()
    override var isDeprecated = localMeta.isDeprecated
    override var isAbstract = localMeta.isAbstract
    override var isSuperEquals = localMeta.isSuperEquals
    override var flattenType: String? = BSMetaHelper.flattenType(this)

    override val allProperties = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BSMetaProperty>()
    override val allExtends = LinkedHashSet<BSGlobalMetaBean>()
    override var metaType = BSMetaType.META_BEAN

    override fun postMerge(globalMetaModel: BSGlobalMetaModel) {
        val extends = BSMetaHelper.getAllExtends(globalMetaModel, this)

        allExtends.addAll(extends)
        extends.forEach {
            allProperties.putAll(it.allProperties)
        }
    }

    override fun mergeInternally(localMeta: BSMetaBean) {
        template ?: let { template = localMeta.template }
        extends ?: let { extends = localMeta.extends }
        description ?: let { description = localMeta.description }
        deprecatedSince ?: let { deprecatedSince = localMeta.deprecatedSince }
        extendsGenericName ?: let { extendsGenericName = localMeta.extendsGenericName }
        genericName ?: let { genericName = localMeta.genericName }
        fullName ?: let { fullName = localMeta.fullName }
        fullExtends ?: let { fullExtends = localMeta.fullExtends }

        if (localMeta.isDeprecated) isDeprecated = localMeta.isDeprecated
        if (localMeta.isAbstract) isAbstract = localMeta.isAbstract
        if (localMeta.isSuperEquals) isSuperEquals = localMeta.isSuperEquals

        localMeta.hints.values
            .filterNot { hints.contains(it.name) }
            .forEach { hints[it.name] = it }

        localMeta.properties.values
            .filterNot { properties.contains(it.name) }
            .forEach { properties[it.name] = it }

        allProperties.putAll(localMeta.properties)
        imports.addAll(localMeta.imports)
        annotations.addAll(localMeta.annotations)
    }

    override fun toString() = "Bean(module=$extensionName, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"
}