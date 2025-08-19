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
package sap.commerce.toolset.typeSystem.codeInspection

import com.intellij.openapi.project.Project
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.model.Deployment

class TSDeploymentTypeCodeReservedInspection : TSDeploymentTypeCodeInspection() {

    override fun applicable(project: Project, dom: Deployment) = dom.typeCode.stringValue
        ?.toIntOrNull()
        ?.let { TSMetaModelAccess.getInstance(project).getReservedTypeCodes().containsKey(it) }
        ?: false

    override fun customMessage(project: Project, dom: Deployment): String? {
        val typeCode = dom.typeCode.stringValue
            ?: return null
        val typeCodeInt = typeCode.toIntOrNull()
            ?: return null
        val reservedType = TSMetaModelAccess.getInstance(project).getReservedTypeCodes()[typeCodeInt]
            ?: return null
        return i18n("hybris.inspections.ts.TSDeploymentTypeCodeReservedInspection.details.key", typeCode, reservedType)
    }
}