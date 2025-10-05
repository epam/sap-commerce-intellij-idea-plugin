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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import java.awt.MouseInfo

internal fun String.toTypeCode() = this
    .substringAfterLast('.')
    .removeSuffix(HybrisConstants.MODEL_SUFFIX)

internal fun getMeta(project: Project, classNameFqn: String): TSGlobalMetaItem? {
    val typeCode = classNameFqn.toTypeCode()

    val meta = TSMetaModelAccess.getInstance(project).findMetaItemByName(typeCode)

    if (meta == null) notifyError("The item type $typeCode is not present in the *items.xml files.")

    return meta
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
