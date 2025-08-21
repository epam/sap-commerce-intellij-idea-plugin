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

package com.intellij.idea.plugin.hybris.debugger.ui.tree.render

import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.tree.render.EnumerationChildrenRenderer
import com.intellij.debugger.ui.tree.render.EnumerationChildrenRenderer.ChildInfo
import com.intellij.idea.plugin.hybris.debugger.TypeRendererUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.application

object ModelEnumerationChildrenRendererInfoProvider {

    fun refreshInfos(
        childrenRenderer: EnumerationChildrenRenderer,
        project: Project,
        className: String,
        fireRenderersChanged: Boolean = false
    ) {
        application.runReadAction {
            val debuggerUtils = DebuggerUtils.getInstance()
            val typeCode = TypeRendererUtils.toTypeCode(className)
            val metaAccess = TSMetaModelAccess.getInstance(project)
            val meta = metaAccess.findMetaItemByName(typeCode)
            if (meta == null) {
                TypeRendererUtils.notifyError(typeCode, TypeRendererUtils.ITEM_TYPE_TS_MISSING)
                return@runReadAction
            }
            val psiClass = DebuggerUtils.findClass(className, project, GlobalSearchScope.allScope(project))
            if (psiClass == null) {
                TypeRendererUtils.notifyError(typeCode, TypeRendererUtils.ITEM_TYPE_CLASS_NOT_FOUND)
                return@runReadAction
            }

            val infos = psiClass.allFields
                .filterNot { it.name.startsWith("_") }
                .mapNotNull {
                    val computedConstantValue = it.computeConstantValue() ?: return@mapNotNull null
                    if (computedConstantValue !is String) return@mapNotNull null

                    meta.allAttributes[computedConstantValue]
                        ?.let { attribute -> return@mapNotNull createChildInfo(attribute, computedConstantValue, it.name, metaAccess, debuggerUtils) }
                    meta.allRelationEnds
                        .filter { relation -> relation.isNavigable }
                        .find { relation -> relation.name == computedConstantValue }
                        ?.let { relation -> return@mapNotNull createChildInfo(computedConstantValue, relation, it.name, debuggerUtils) }
                }
                .sortedBy { it.myName }

            childrenRenderer.children = infos

            if (fireRenderersChanged) NodeRendererSettings.getInstance().fireRenderersChanged()
        }
    }

    private fun createChildInfo(
        attributeName: String,
        relation: TSMetaRelation.TSMetaRelationElement,
        fieldName: String,
        debuggerUtils: DebuggerUtils
    ) = ChildInfo(
        "$attributeName (relation - ${relation.end.name.lowercase()})",
        debuggerUtils.createExpressionWithImports("getProperty($fieldName)"),
        true
    )

    private fun createChildInfo(
        attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute,
        attributeName: String,
        fieldName: String,
        metaAccess: TSMetaModelAccess,
        debuggerUtils: DebuggerUtils
    ): ChildInfo {
        val expression = debuggerUtils.createExpressionWithImports("getProperty($fieldName)")

        if (attribute.isDynamic)
            return ChildInfo("$attributeName (dynamic)", expression, true)
        if (metaAccess.findMetaCollectionByName(attribute.type) != null)
            return ChildInfo("$attributeName (collection)", expression, true)
        if (metaAccess.findMetaMapByName(attribute.type) != null)
            return ChildInfo("$attributeName (map)", expression, true)

        return ChildInfo(attributeName, expression, false)
    }
}