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
package com.intellij.idea.plugin.hybris.beans.meta.model.impl

import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaHelper
import com.intellij.idea.plugin.hybris.beans.meta.model.*
import com.intellij.idea.plugin.hybris.beans.model.Bean
import com.intellij.idea.plugin.hybris.beans.model.BeanType
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BeansMetaBeanImpl(
    dom: Bean,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean,
    override val imports: List<BeansMetaImport>,
    override val annotations: List<BeansMetaAnnotations>,
    override val properties: Map<String, BeansMetaProperty>,
    override val hints: Map<String, BeansMetaHint>,
) : BeansMetaBean {

    override val domAnchor: DomAnchor<Bean> = DomService.getInstance().createAnchor(dom)
    override val description = dom.description.stringValue
    override val template = dom.template.stringValue
    override val extends = dom.extends.stringValue
    override val type = dom.type.value ?: BeanType.BEAN
    override val shortName = BeansMetaHelper.getShortName(name)
    override val deprecatedSince = dom.deprecatedSince.stringValue
    override val isDeprecated = java.lang.Boolean.TRUE == dom.deprecated.value
    override val isAbstract = java.lang.Boolean.TRUE == dom.abstract.value
    override val isSuperEquals = java.lang.Boolean.TRUE == dom.superEquals.value

    override fun toString() = "Bean(module=$module, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"

}

internal class BeansGlobalMetaBeanImpl(localMeta: BeansMetaBean)
    : BeansMetaSelfMerge<Bean, BeansMetaBean>(localMeta), BeansGlobalMetaBean {

    override val hints = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BeansMetaHint>()
    override val properties = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BeansMetaProperty>()
    override val domAnchor = localMeta.domAnchor
    override val type = localMeta.type
    override val shortName = localMeta.shortName
    override val module = localMeta.module
    override var template = localMeta.template
    override var description = localMeta.description
    override var deprecatedSince = localMeta.deprecatedSince
    override var extends = localMeta.extends
    override val imports = ArrayList<BeansMetaImport>()
    override val annotations = ArrayList<BeansMetaAnnotations>()
    override var isDeprecated = localMeta.isDeprecated
    override var isAbstract = localMeta.isAbstract
    override var isSuperEquals = localMeta.isSuperEquals

    override fun mergeInternally(localMeta: BeansMetaBean) {
        template?:let { template = localMeta.template }
        extends?:let { extends = localMeta.extends }
        description?:let { description = localMeta.description }
        deprecatedSince?:let { deprecatedSince = localMeta.deprecatedSince }

        if (localMeta.isDeprecated) isDeprecated = localMeta.isDeprecated
        if (localMeta.isAbstract) isAbstract = localMeta.isAbstract
        if (localMeta.isSuperEquals) isSuperEquals = localMeta.isSuperEquals

        localMeta.hints.values
            .filterNot { hints.contains(it.name) }
            .forEach { hints[it.name] = it }

        localMeta.properties.values
            .filterNot { properties.contains(it.name) }
            .forEach { properties[it.name] = it }

        imports.addAll(localMeta.imports)
        annotations.addAll(localMeta.annotations)
    }

    override fun toString() = "Bean(module=$module, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"
}