/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.typeSystem.psi.reference.result

import com.intellij.psi.ResolveResult
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

object TSResolveResultUtil {

    fun isLocalized(resolveResult: ResolveResult, featureName: String) =
        (resolveResult is AttributeResolveResult && resolveResult.meta.isLocalized)
            || (resolveResult is RelationEndResolveResult && resolveResult.meta.owner.isLocalized)
            || (resolveResult is EnumResolveResult && featureName == TSConstants.Attribute.NAME)

    fun tryResolveAttribute(
        metaModelAccess: TSMetaModelAccess,
        refName: String,
        itemTypeCode: String
    ): ResolveResult? = tryResolveForItemType(metaModelAccess, refName, itemTypeCode)
        ?: tryResolveForRelationType(metaModelAccess, refName, itemTypeCode)
        ?: tryResolveByEnumType(metaModelAccess, refName, itemTypeCode)

    private fun tryResolveByEnumType(
        metaModelAccess: TSMetaModelAccess,
        refName: String,
        itemTypeCode: String
    ): ResolveResult? = metaModelAccess.findMetaEnumByName(itemTypeCode)
        ?.let { metaModelAccess.findMetaItemByName(TSConstants.Type.ENUMERATION_VALUE) }
        ?.let { it.allAttributes[refName] }
        ?.let { AttributeResolveResult(it) }

    private fun tryResolveForItemType(
        metaModelService: TSMetaModelAccess,
        featureName: String,
        itemTypeCode: String
    ): ResolveResult? = metaModelService.findMetaItemByName(itemTypeCode)
        ?.let { meta ->
            meta.allAttributes[featureName]
                ?.let { AttributeResolveResult(it) }
                ?: meta.allOrderingAttributes[featureName]
                    ?.let { OrderingAttributeResolveResult(it) }
                ?: meta.allRelationEnds
                    .find { it.name.equals(featureName, true) }
                    ?.let { RelationEndResolveResult(it) }
        }

    private fun tryResolveForRelationType(
        metaModelAccess: TSMetaModelAccess,
        featureName: String,
        itemTypeCode: String
    ): ResolveResult? = metaModelAccess.findMetaRelationByName(itemTypeCode)
        ?.let {
            if (TSConstants.Attribute.SOURCE.equals(featureName, ignoreCase = true)) {
                return@let RelationEndResolveResult(it.source)
            } else if (TSConstants.Attribute.TARGET.equals(featureName, ignoreCase = true)) {
                return@let RelationEndResolveResult(it.target)
            }

            return@let tryResolveForItemType(metaModelAccess, featureName, TSConstants.Type.LINK)
        }
}