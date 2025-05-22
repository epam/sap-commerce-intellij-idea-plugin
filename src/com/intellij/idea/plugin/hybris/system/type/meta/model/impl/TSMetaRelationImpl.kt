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
package com.intellij.idea.plugin.hybris.system.type.meta.model.impl

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation.*
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.idea.plugin.hybris.system.type.model.RelationElement
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaRelationImpl(
    dom: Relation,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override var isCustom: Boolean,
    override val source: TSMetaRelationElement,
    override val target: TSMetaRelationElement,
    override val orderingAttribute: TSMetaOrderingAttribute?,
    override val deployment: TSMetaDeployment?
) : TSMetaRelation {

    override val domAnchor: DomAnchor<Relation> = DomService.getInstance().createAnchor(dom)
    override val isLocalized = java.lang.Boolean.TRUE == dom.localized.value
    override val isAutoCreate = dom.autoCreate.toBoolean()
    override val isGenerate = dom.generate.toBoolean()
    override val description = dom.description.stringValue

    init {
        source.owner = this
        target.owner = this
    }

    override fun toString() = "Relation(module=$extensionName, name=$name, isCustom=$isCustom)"

    internal class TSMetaRelationElementImpl(
        dom: RelationElement,
        override val moduleName: String,
        override val extensionName: String,
        override var isCustom: Boolean,
        override val end: RelationEnd,
        override val modifiers: TSMetaModifiers,
        override val customProperties: Map<String, TSMetaCustomProperty>
    ) : TSMetaRelationElement {

        override lateinit var owner: TSMetaRelation

        override val domAnchor: DomAnchor<RelationElement> = DomService.getInstance().createAnchor(dom)

        override val type = dom.type.stringValue ?: ""
        override val qualifier = dom.qualifier.stringValue
        override val name = qualifier
        override val isNavigable = dom.navigable.toBoolean()
        override val isOrdered = dom.ordered.toBoolean()
        override val isDeprecated = TSMetaHelper.isDeprecated(dom.model, name)
        override val collectionType = dom.collectionType.value
        override val cardinality = dom.cardinality.value
        override val description = dom.description.stringValue
        override val metaType = dom.metaType.stringValue
        // type will be flattened after merge, we need to know exact type to expand it
        override var flattenType: String? = null

        override fun toString() = "RelationElement(module=$extensionName, name=$name, isCustom=$isCustom)"
    }

    internal class TSMetaOrderingAttributeImpl(
        dom: RelationElement,
        override var owner: TSMetaRelationElement,
        override val moduleName: String,
        override val extensionName: String,
        override var isCustom: Boolean,
        override var qualifier: String
    ) : TSMetaOrderingAttribute {

        override val domAnchor: DomAnchor<RelationElement> = DomService.getInstance().createAnchor(dom)

        override val name = qualifier
        override var type: String = "int"
        override var flattenType: String? = "int"

        override fun toString() = "RelationOrderingAttribute(module=$extensionName, name=$name, isCustom=$isCustom)"
    }
}

internal class TSGlobalMetaRelationImpl(localMeta: TSMetaRelation)
    : TSMetaSelfMerge<Relation, TSMetaRelation>(localMeta), TSGlobalMetaRelation {

    override val domAnchor = localMeta.domAnchor
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override var isLocalized = localMeta.isLocalized
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var description = localMeta.description
    override var deployment = localMeta.deployment
    override var source = localMeta.source
    override var target = localMeta.target
    override val orderingAttribute = localMeta.orderingAttribute
    override var flattenType: String? = TSMetaHelper.flattenType(this)

    override fun mergeInternally(localMeta: TSMetaRelation) = Unit

    override fun toString() = "Relation(module=$extensionName, name=$name, isCustom=$isCustom)"

}