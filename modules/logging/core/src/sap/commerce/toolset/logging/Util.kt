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

package sap.commerce.toolset.logging

import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.bundled.CxBundledLogTemplate
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLogTemplateState
import sap.commerce.toolset.logging.presentation.CxLogTemplatePresentation
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation

fun CxBundledLogTemplate.presentation() = CxLogTemplatePresentation(
    name = name,
    loggers = loggers.map { presentation(it.identifier, it.effectiveLevel) },
    icon = iconName
        ?.let { iconsMap.getOrElse(it) { HybrisIcons.Log.Template.DEFAULT } }
        ?: HybrisIcons.Log.Template.DEFAULT
)

fun CxCustomLogTemplateState.presentation() = CxLogTemplatePresentation(
    uuid = uuid,
    name = name,
    loggers = loggers.map { presentation(it.name, it.effectiveLevel.name) },
    icon = HybrisIcons.Log.Template.CUSTOM_TEMPLATE,
    defaultLogLevel = defaultEffectiveLevel
)

private fun presentation(
    identifier: String,
    level: String,
) = CxLoggerPresentation.of(
    name = identifier,
    effectiveLevel = level,
)

private val iconsMap = mapOf(
    "DISABLE" to HybrisIcons.Log.Template.DISABLE,
    "ENABLE" to HybrisIcons.Log.Template.ENABLE
)
