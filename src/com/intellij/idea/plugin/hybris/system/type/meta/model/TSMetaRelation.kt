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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.system.type.model.Cardinality
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.idea.plugin.hybris.system.type.model.RelationElement
import com.intellij.idea.plugin.hybris.system.type.model.Type

interface TSMetaRelation : TSMetaClassifier<Relation> {
    val deployment: TSMetaDeployment
    val source: TSMetaRelationElement
    val target: TSMetaRelationElement
    val description: String?
    val isLocalized: Boolean
    val isAutoCreate: Boolean
    val isGenerate: Boolean

    interface TSMetaRelationElement : TSMetaClassifier<RelationElement>, TSTypedClassifier {
        var owner: TSMetaRelation
        val end: RelationEnd
        val qualifier: String
        val type: String
        val modifiers: TSMetaModifiers
        val customProperties: Map<String, TSMetaCustomProperty>
        val collectionType: Type
        val cardinality: Cardinality?
        val description: String?
        val metaType: String?
        val isOrdered: Boolean
        val isNavigable: Boolean
        val isDeprecated: Boolean
    }

    enum class RelationEnd {
        SOURCE, TARGET
    }
}

interface TSGlobalMetaRelation : TSMetaRelation, TSGlobalMetaClassifier<Relation>, TSTypedClassifier {
    override val declarations: MutableSet<TSMetaRelation>
}
