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

import com.intellij.util.ui.EmptyIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService

object ImpExRowColumnTypes {

    private val UNKNOWN_ICON = EmptyIcon.ICON_16

    /**
     * The type of each column of the value line: a link for a document id declaration, otherwise the type of the attribute.
     * It is unknown, if the type system is not ready or the attribute cannot be resolved.
     */
    fun resolve(valueLine: ImpExValueLine): List<ImpExRowColumnType> {
        val project = valueLine.project
        // resolving of the attributes requires the meta model, which cancels the read action while it is being calculated
        val access = TSMetaModelStateService.getInstance(project)
            .takeIf { it.initialized() }
            ?.let { TSMetaModelAccess.getInstance(project) }

        return valueLine.valueGroupList.map { group ->
            val parameter = group.fullHeaderParameter

            if (parameter?.anyHeaderParameterName?.documentIdDec != null) {
                return@map ImpExRowColumnType(HybrisIcons.ImpEx.DOC_ID_LINK, DOCUMENT_ID)
            }

            val typeName = access?.let { parameter?.typeSystemContext?.attributeType }
            val classifier = typeName?.let { access.findMetaClassifierByName(it) }

            ImpExRowColumnType(classifier?.icon ?: UNKNOWN_ICON, typeName)
        }
    }

    private const val DOCUMENT_ID = "Document ID"
}
