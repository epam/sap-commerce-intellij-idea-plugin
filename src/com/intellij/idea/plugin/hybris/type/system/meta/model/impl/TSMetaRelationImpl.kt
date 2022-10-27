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
package com.intellij.idea.plugin.hybris.type.system.meta.model.impl

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaDeployment
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaModifiers
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation.RelationEnd
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation.TSMetaRelationElement
import com.intellij.idea.plugin.hybris.type.system.model.Relation
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement
import com.intellij.idea.plugin.hybris.type.system.model.Type
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

class TSMetaRelationImpl(
    override val module: Module,
    override val project: Project,
    override val name: String?,
    dom: Relation,
    override val isCustom: Boolean
) : TSMetaEntityImpl<Relation>(dom, module, project, isCustom, name), TSMetaRelation {

    override val isLocalized = java.lang.Boolean.TRUE == dom.localized.value
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val description = dom.description.stringValue
    override val deployment: TSMetaDeployment<TSMetaRelation> = TSMetaDeploymentImpl(module, project, dom.deployment, isCustom, this, TSMetaModelNameProvider.extract(dom.deployment))
    override val source: TSMetaRelationElement = TSMetaRelationElementImpl(module, project, dom.sourceElement, isCustom, this, RelationEnd.SOURCE)
    override val target: TSMetaRelationElement = TSMetaRelationElementImpl(module, project, dom.targetElement, isCustom, this, RelationEnd.TARGET)

    override fun toString(): String {
        return "TSMetaRelationImpl(module=$module, name=$name, isCustom=$isCustom)"
    }

    private class TSMetaRelationElementImpl(
        override val module: Module,
        override val project: Project,
        dom: RelationElement,
        override val isCustom: Boolean,
        override val owner: TSMetaRelation,
        override val end: RelationEnd
    ) : TSMetaEntityImpl<RelationElement>(dom, module, project, isCustom), TSMetaRelationElement {

        override val customProperties: ConcurrentHashMap<String, TSMetaCustomProperty> = CaseInsensitiveConcurrentHashMap()
        override val type = dom.type.stringValue ?: ""
        override val qualifier = dom.qualifier.stringValue ?: ""
        override val name = qualifier
        override val isNavigable = dom.navigable.value ?: true
        override val isOrdered = java.lang.Boolean.TRUE == dom.ordered.value
        override val collectionType = dom.collectionType.value ?: Type.COLLECTION
        override val cardinality = dom.cardinality.value
        override val description = dom.description.stringValue
        override val metaType = dom.metaType.stringValue
        override val modifiers: TSMetaModifiers<TSMetaRelationElement> = TSMetaModifiersImpl(module, project, dom.modifiers, isCustom)

        init {
            dom.customProperties.properties.stream()
                .filter { TSMetaModelNameProvider.extract(it) != null}
                .map { TSMetaCustomPropertyImpl(module, project, it, isCustom, TSMetaModelNameProvider.extract(it)!!) }
                .forEach { customProperties[it.name] = it }
        }

        override fun toString(): String {
            return "TSMetaRelationElementImpl(module=$module, name=$name, isCustom=$isCustom)"
        }
    }
}