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

package com.intellij.idea.plugin.hybris.debugger

import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.application
import java.awt.MouseInfo

object TypeRendererUtils {

    val ITEM_TYPE_TS_MISSING = { typeCode: String -> "The item type $typeCode is not present in the *items.xml files." }
    val ITEM_TYPE_CLASS_NOT_FOUND = { typeCode: String -> "The class for the item type $typeCode was not found. Rebuild the project and try again." }

    fun toTypeCode(className: String) = className.substringAfterLast('.').removeSuffix("Model")

    fun notifyError(typeCode: String, messageFunc: (String) -> String) = notifyError(messageFunc(typeCode))

    fun notifyError(errorMessage: String) {
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
}