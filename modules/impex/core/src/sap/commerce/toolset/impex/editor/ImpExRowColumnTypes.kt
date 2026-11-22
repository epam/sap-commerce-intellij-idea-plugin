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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.util.ui.EmptyIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResult

object ImpExRowColumnTypes {

    private val UNKNOWN_ICON = EmptyIcon.ICON_16

    /**
     * The type of each column of the header: a link for a document id declaration, otherwise the type of the attribute.
     * It is unknown, if the type system is not ready or the attribute cannot be resolved.
     */
    fun resolve(valueLine: ImpExValueLine): List<ImpExRowColumnType> {
        val project = valueLine.project
        // resolving of the attributes requires the meta model, which cancels the read action while it is being calculated
        val access = TSMetaModelStateService.getInstance(project)
            .takeIf { it.initialized() && !DumbService.isDumb(project) }
            ?.let { TSMetaModelAccess.getInstance(project) }

        return valueLine.headerLine?.fullHeaderParameterList.orEmpty().map { parameter ->
            if (parameter.isDocumentIdDeclaration) {
                ImpExRowColumnType(HybrisIcons.ImpEx.DOC_ID_LINK, i18n("hybris.impex.editor.row_mode.type.document_id"))
            } else {
                access?.let { resolveAttributeType(it, parameter) }
                    ?: ImpExRowColumnType(UNKNOWN_ICON, null)
            }
        }
    }

    private fun resolveAttributeType(access: TSMetaModelAccess, parameter: ImpExFullHeaderParameter): ImpExRowColumnType? {
        // the same resolution as for the navigation to the declaration of the attribute
        val meta = parameter
            .anyHeaderParameterName
            .references
            .asSequence()
            .filterIsInstance<PsiPolyVariantReference>()
            .flatMap { it.multiResolve(false).asSequence() }
            .filterIsInstance<TSResolveResult<*>>()
            .map { it.meta }
            .firstOrNull()
            ?: return null

        val typeName = when (meta) {
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> meta.type
            is TSMetaRelation.TSMetaRelationElement -> meta.type
            else -> null
        }
        val attributeIcon = when (meta) {
            is TSGlobalMetaClassifier<*> -> meta.icon
            is TSMetaRelation.TSMetaRelationElement -> meta.end.icon
            else -> null
        }
        // the icon of the type if it is known, otherwise the icon of the attribute itself (regular, localized, dynamic)
        val icon = typeName
            ?.removePrefix(TSConstants.Attribute.LOCALIZED_PREFIX)
            ?.let { access.findMetaClassifierByName(it) }
            ?.icon
            ?: attributeIcon
            ?: return null

        return ImpExRowColumnType(icon, typeName)
    }
}
