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
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation.RelationEnd
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation.TSMetaRelationElement
import com.intellij.idea.plugin.hybris.type.system.model.Relation
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement
import com.intellij.idea.plugin.hybris.type.system.model.Type
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService
import java.util.concurrent.ConcurrentHashMap


class TSMetaRelationImpl(
    dom: Relation,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean
) : TSMetaRelation {

    override val domAnchor: DomAnchor<Relation> = DomService.getInstance().createAnchor(dom)
    override val isLocalized = java.lang.Boolean.TRUE == dom.localized.value
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val description = dom.description.stringValue
    override val deployment: TSMetaDeployment<TSMetaRelation> = TSMetaDeploymentImpl(dom.deployment, this, module, TSMetaModelNameProvider.extract(dom.deployment), isCustom)
    override val source: TSMetaRelation.TSMetaRelationElement = TSMetaRelationElementImpl(dom.sourceElement, this, module, isCustom, RelationEnd.SOURCE)
    override val target: TSMetaRelation.TSMetaRelationElement = TSMetaRelationElementImpl(dom.targetElement, this, module, isCustom, RelationEnd.TARGET)

    override fun toString() = "TSMetaRelationImpl(module=$module, name=$name, isCustom=$isCustom)"

    private class TSMetaRelationElementImpl(
        dom: RelationElement,
        override val owner: TSMetaRelation,
        override val module: Module,
        override val isCustom: Boolean,
        override val end: RelationEnd
    ) : TSMetaRelation.TSMetaRelationElement {

        override val domAnchor: DomAnchor<RelationElement> = DomService.getInstance().createAnchor(dom)
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

        override val modifiers: TSMetaModifiers<TSMetaRelation.TSMetaRelationElement> = TSMetaModifiersImpl(dom.modifiers, module, isCustom)

        init {
            dom.customProperties.properties.stream()
                .filter { TSMetaModelNameProvider.extract(it) != null}
                .map { TSMetaCustomPropertyImpl(it, module, isCustom, TSMetaModelNameProvider.extract(it)!!) }
                .forEach { customProperties[it.name] = it }
        }

        override fun toString() = "TSMetaRelationElementImpl(module=$module, name=$name, isCustom=$isCustom)"
    }
}

class TSGlobalMetaRelationImpl(localMeta: TSMetaRelation)
    : TSMetaSelfMerge<Relation, TSMetaRelation>(localMeta), TSGlobalMetaRelation {

    override val domAnchor = localMeta.domAnchor
    override val module = localMeta.module
    override var isLocalized = localMeta.isLocalized
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var description = localMeta.description
    override var deployment: TSMetaDeployment<TSMetaRelation> = localMeta.deployment
    override var source: TSMetaRelationElement = localMeta.source
    override var target: TSMetaRelationElement = localMeta.target

    override fun mergeInternally(localMeta: TSMetaRelation) = Unit

}