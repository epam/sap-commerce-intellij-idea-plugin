/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.lang.documentation.renderer.hybrisDoc
import com.intellij.idea.plugin.hybris.system.type.model.Cardinality
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.idea.plugin.hybris.system.type.model.RelationElement
import com.intellij.idea.plugin.hybris.system.type.model.Type

interface TSMetaRelation : TSMetaClassifier<Relation> {
    val deployment: TSMetaDeployment?
    val source: TSMetaRelationElement
    val target: TSMetaRelationElement
    val orderingAttribute: TSMetaOrderingAttribute?
    val description: String?
    val isLocalized: Boolean
    val isAutoCreate: Boolean
    val isGenerate: Boolean

    interface TSMetaRelationElement : TSMetaClassifier<RelationElement>, TSTypedClassifier {
        var owner: TSMetaRelation
        val end: RelationEnd
        val qualifier: String?
        val type: String
        val modifiers: TSMetaModifiers
        val customProperties: Map<String, TSMetaCustomProperty>
        val collectionType: Type
        val cardinality: Cardinality
        val description: String?
        val metaType: String?
        val isOrdered: Boolean
        val isNavigable: Boolean
        val isDeprecated: Boolean

        override fun documentation() = hybrisDoc {
            texts(
                "<strong>${end.name.lowercase()}</strong> (${cardinality.value} $type)",
                "qualifier :: ${qualifier ?: "<no qualifier>"}",
                flagDocumentation(),
                modifiers.documentation()
            )
        }.build()

        private fun flagDocumentation() = listOfNotNull(
            if (isOrdered) "ordered" else null,
            if (isNavigable) "navigable" else null,
            if (isDeprecated) "isDeprecated" else null
        ).joinToString(",&nbsp;")
    }

    interface TSMetaOrderingAttribute : TSMetaClassifier<RelationElement>, TSTypedClassifier {
        override val name: String
        var owner: TSMetaRelationElement
        var qualifier: String
        var type: String
    }

    enum class RelationEnd {
        SOURCE, TARGET
    }
}

interface TSGlobalMetaRelation : TSMetaRelation, TSGlobalMetaClassifier<Relation>, TSTypedClassifier {
    override val declarations: MutableSet<TSMetaRelation>

    override fun documentation() = hybrisDoc {
        title("Relation type", name ?: "?")
        modifiersDocumentation()
            .takeIf { it.isNotBlank() }
            ?.let { subHeader(it) }
        deployment
            ?.documentation()
            ?.let { contentsWithSeparator(it) }
        contents(
            source.documentation(),
            target.documentation(),
        )
    }.build()

    private fun modifiersDocumentation() = listOfNotNull(
        if (isLocalized) "localized" else null,
        if (isAutoCreate) "autocreate" else null,
        if (isGenerate) "generate" else null
    ).joinToString(",&nbsp;")

}
