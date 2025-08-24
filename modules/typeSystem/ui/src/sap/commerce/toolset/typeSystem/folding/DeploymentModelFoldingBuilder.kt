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
package sap.commerce.toolset.typeSystem.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag
import sap.commerce.toolset.folding.XmlFoldingBuilderEx
import sap.commerce.toolset.settings.state.TypeSystemFoldingSettingsState
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.typeSystem.model.deployment.DatabaseSchema
import sap.commerce.toolset.typeSystem.model.deployment.Model
import sap.commerce.toolset.typeSystem.model.deployment.TypeMapping

class DeploymentModelFoldingBuilder : XmlFoldingBuilderEx<TypeSystemFoldingSettingsState, Model>(Model::class.java), DumbAware {

    override val filter = PsiElementFilter {
        when (it) {
            is XmlTag -> when (it.localName) {
                DatabaseSchema.TYPE_MAPPING -> true

                else -> false
            }

            else -> false
        }
    }

    override fun initSettings(project: Project) = project.yDeveloperSettings
        .typeSystemSettings
        .folding

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            DatabaseSchema.TYPE_MAPPING -> (psi.getAttributeValue(TypeMapping.TYPE)
                ?.let { tablify(psi, it, true, DatabaseSchema.TYPE_MAPPING, TypeMapping.TYPE) }
                ?: FALLBACK_PLACEHOLDER) + psi.getAttributeValue(TypeMapping.PERSISTENCE_TYPE)
                ?.let { " $it" }

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            DatabaseSchema.TYPE_MAPPING -> true

            else -> false
        }

        else -> false
    }

}
