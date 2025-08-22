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

package sap.commerce.toolset.debugger

import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.tree.render.EnumerationChildrenRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation
import java.awt.MouseInfo

internal fun createRendererName(className: String) = "${HybrisConstants.DEBUG_MODEL_RENDERER_PREFIX} ${className.substringAfterLast('.')}"

internal fun getMeta(project: Project, classNameFqn: String): TSGlobalMetaItem? {
    val typeCode = classNameFqn.toTypeCode()

    val meta = TSMetaModelAccess.getInstance(project).findMetaItemByName(typeCode)

    if (meta == null) notifyError("The item type $typeCode is not present in the *items.xml files.")

    return meta
}

internal fun findClass(project: Project, classNameFqn: String): PsiClass? {
    val psiClass = DebuggerUtils.findClass(classNameFqn, project, GlobalSearchScope.allScope(project))

    if (psiClass == null) {
        val typeCode = classNameFqn.toTypeCode()
        notifyError("The class for the item type $typeCode was not found. Rebuild the project and try again.")
    }

    return psiClass
}

internal fun refreshInfos(
    childrenRenderer: EnumerationChildrenRenderer,
    project: Project,
    classNameFqn: String,
    fireRenderersChanged: Boolean = false
) {
    application.runReadAction {
        val debuggerUtils = DebuggerUtils.getInstance()
        val meta = getMeta(project, classNameFqn) ?: return@runReadAction
        val psiClass = findClass(project, classNameFqn) ?: return@runReadAction
        val metaAccess = TSMetaModelAccess.getInstance(project)

        val infos = psiClass.allFields
            .filterNot { it.name.startsWith("_") }
            .mapNotNull {
                val computedConstantValue = it.computeConstantValue()
                    ?.asSafely<String>()
                    ?: return@mapNotNull null

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
) = EnumerationChildrenRenderer.ChildInfo(
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
) = with(debuggerUtils.createExpressionWithImports("getProperty($fieldName)")) {
    when {
        attribute.isDynamic -> EnumerationChildrenRenderer.ChildInfo("$attributeName (dynamic)", this, true)
        metaAccess.findMetaCollectionByName(attribute.type) != null -> EnumerationChildrenRenderer.ChildInfo("$attributeName (collection)", this, true)
        metaAccess.findMetaMapByName(attribute.type) != null -> EnumerationChildrenRenderer.ChildInfo("$attributeName (map)", this, true)
        else -> EnumerationChildrenRenderer.ChildInfo(attributeName, this, false)
    }
}

private fun notifyError(errorMessage: String) {
    application.invokeLater {
        val mouseLoc = MouseInfo.getPointerInfo()?.location ?: return@invokeLater
        val balloon = JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(errorMessage, MessageType.ERROR, null)
            .setFadeoutTime(5000)
            .setHideOnClickOutside(true)
            .setHideOnKeyOutside(true)
            .setCloseButtonEnabled(false)
            .createBalloon()

        balloon.show(RelativePoint.fromScreen(mouseLoc), Balloon.Position.above)
    }
}

private fun String.toTypeCode() = this
    .substringAfterLast('.')
    .removeSuffix(HybrisConstants.MODEL_SUFFIX)
