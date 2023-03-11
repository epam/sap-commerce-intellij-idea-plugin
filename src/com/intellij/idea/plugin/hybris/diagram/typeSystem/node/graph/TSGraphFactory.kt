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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.CollectionType
import com.intellij.idea.plugin.hybris.system.type.model.Deployment
import com.intellij.idea.plugin.hybris.system.type.model.MapType
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

object TSGraphFactory {

    fun buildNode(project: Project, name: String) = TSMetaModelAccess.getInstance(project).findMetaClassifierByName(name)
        ?.takeIf { it.name != null }
        ?.let { buildNode(it.name, it) }

    fun buildNode(meta: TSGlobalMetaClassifier<out DomElement>) = when (meta) {
        is TSGlobalMetaEnum -> buildNode(meta)
        is TSGlobalMetaCollection -> buildNode(meta)
        is TSGlobalMetaRelation -> buildNode(meta)
        is TSGlobalMetaMap -> buildNode(meta)
        is TSGlobalMetaItem -> buildNode(meta)
        else -> buildNode(meta.name, meta)
    }

    fun buildNode(meta: TSGlobalMetaItem): TSGraphNodeClassifier {
        val deploymentProperties = deploymentFields(meta.deployment)
        val customProperties = meta.customProperties.entries
            .map { (name, customProperty) -> TSGraphFieldCustomProperty(name, customProperty) }
            .sortedBy { it.name }
        val attributes = meta.attributes.entries
            .map { (name, attribute) -> TSGraphFieldAttribute(name, attribute) }
            .sortedBy { it.name }
        val relationEnds = meta.relationEnds
            .filter { it.name != null }
            .map { TSGraphFieldRelationEnd(it.name!!, it) }
            .sortedWith(compareBy({ it.meta.end }, { it.name }))
        val indexes = meta.indexes.entries
            .map { (name, index) -> TSGraphFieldIndex(name, index) }
            .sortedBy { it.name }

        val fields = deploymentProperties + customProperties + attributes + relationEnds + indexes
        return buildNode(meta.name, meta, fields)
    }

    fun buildNode(meta: TSGlobalMetaMap): TSGraphNodeClassifier {
        val properties = listOf(
            TSGraphFieldProperty(MapType.ARGUMENTTYPE, meta.argumentType ?: "?"),
            TSGraphFieldProperty(MapType.RETURNTYPE, meta.returnType ?: "?"),
        )

        return buildNode(meta.name, meta, properties.toTypedArray())
    }

    fun buildNode(meta: TSGlobalMetaRelation): TSGraphNodeClassifier {
        val deploymentFields = deploymentFields(meta.deployment)
        val properties = listOf(
            TSGraphFieldRelationElement(Relation.SOURCE_ELEMENT, meta.source),
            TSGraphFieldRelationElement(Relation.TARGET_ELEMENT, meta.target),
        )

        val fields = deploymentFields + properties
        return buildNode(meta.name, meta, fields)
    }

    fun buildNode(meta: TSGlobalMetaCollection): TSGraphNodeClassifier {
        val fields = listOf(
            TSGraphFieldProperty(CollectionType.TYPE, meta.type.value),
            TSGraphFieldProperty(CollectionType.ELEMENTTYPE, meta.elementType),
        )

        return buildNode(meta.name, meta, fields.toTypedArray())
    }

    fun buildNode(meta: TSGlobalMetaEnum): TSGraphNodeClassifier {
        val properties = listOf(
            TSGraphFieldProperty(HybrisConstants.CODE_ATTRIBUTE_NAME, "String"),
            TSGraphFieldProperty(HybrisConstants.NAME_ATTRIBUTE_NAME, "String"),
        )

        val values = meta.values.entries
            .map { (name, metaEnumValue) -> TSGraphFieldEnumValue(name, metaEnumValue) }
            .sortedBy { it.name }

        return buildNode(meta.name, meta, (properties + values).toTypedArray())
    }

    private fun deploymentFields(deployment: TSMetaDeployment?): Array<TSGraphField> {
        val deploymentProperties = deployment?.let {
            arrayOf<TSGraphField>(
                TSGraphFieldDeployment(Deployment.TYPE_CODE, it.typeCode ?: "?"),
                TSGraphFieldDeployment(Deployment.TABLE, it.table ?: "?"),
                TSGraphFieldDeployment(Deployment.PROPERTY_TABLE, it.propertyTable
                    ?: "props"),
            )
        } ?: emptyArray()
        return deploymentProperties
    }

    private fun buildNode(
        name: String?,
        meta: TSGlobalMetaClassifier<out DomElement>,
        fields: Array<TSGraphField> = emptyArray()
    ) = TSGraphNodeClassifier(name!!, meta, fields)

}