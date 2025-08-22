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

package sap.commerce.toolset.typeSystem.diagram

import com.intellij.diagram.DiagramScopeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.NamedScope
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNode
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNodeClassifier

class TSDiagramScopeManager(project: Project) : DiagramScopeManager<TSGraphNode>(project) {

    init {
        currentScope = scopeCustomExtends
    }

    override fun getScopes() = allowedScopes

    override fun contains(graphNode: TSGraphNode?): Boolean {
        val scope = currentScope ?: return true
        if (scope == scopeAll) return true
        if (graphNode !is TSGraphNodeClassifier) return true

        val isCustom = graphNode.meta.isCustom

        return (scope == scopeCustom && isCustom)
            || (scope == scopeCustomExtends && (isCustom || graphNode.transitiveNode))
            || (scope == scopeOOTB && !isCustom)
    }

    companion object {
        private const val SCOPE_ID_CUSTOM = "Custom"
        private const val SCOPE_ID_CUSTOM_WITH_EXTENDS = "CustomWithExtends"
        private const val SCOPE_ID_OOTB = "OOTB"
        private const val SCOPE_ID_ALL = "All"

        private val scopeCustom = NamedScope(SCOPE_ID_CUSTOM, { i18n("hybris.diagram.ts.provider.scope.custom.only_custom") }, HybrisIcons.Extension.CUSTOM, null)
        private val scopeCustomExtends = NamedScope(SCOPE_ID_CUSTOM_WITH_EXTENDS, { i18n("hybris.diagram.ts.provider.scope.custom.custom_with_extends") }, HybrisIcons.Extension.CUSTOM, null)
        private val scopeOOTB = NamedScope(SCOPE_ID_OOTB, { i18n("hybris.diagram.ts.provider.scope.custom.ootb") }, HybrisIcons.Extension.OOTB, null)
        private val scopeAll = NamedScope(SCOPE_ID_ALL, HybrisIcons.TypeSystem.FILE, null)

        private val allowedScopes = arrayOf(
            scopeCustom,
            scopeCustomExtends,
            scopeOOTB,
            scopeAll
        )
    }
}