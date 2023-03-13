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

package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.DiagramScopeManager
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNode
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNodeClassifier
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.NamedScope

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
        const val SCOPE_ID_CUSTOM = "Custom"
        const val SCOPE_ID_CUSTOM_WITH_EXTENDS = "CustomWithExtends"
        const val SCOPE_ID_OOTB = "OOTB"
        const val SCOPE_ID_ALL = "All"

        private val scopeCustom = NamedScope(SCOPE_ID_CUSTOM, { message("hybris.diagram.ts.provider.scope.custom.only_custom") }, HybrisIcons.EXTENSION_CUSTOM, null)
        private val scopeCustomExtends = NamedScope(SCOPE_ID_CUSTOM_WITH_EXTENDS, { message("hybris.diagram.ts.provider.scope.custom.custom_with_extends") }, HybrisIcons.EXTENSION_CUSTOM, null)
        private val scopeOOTB = NamedScope(SCOPE_ID_OOTB, { message("hybris.diagram.ts.provider.scope.custom.ootb") }, HybrisIcons.EXTENSION_OOTB, null)
        private val scopeAll = NamedScope(SCOPE_ID_ALL, HybrisIcons.TYPE_SYSTEM, null)

        private val allowedScopes = arrayOf(
            scopeCustom,
            scopeCustomExtends,
            scopeOOTB,
            scopeAll
        )
    }
}