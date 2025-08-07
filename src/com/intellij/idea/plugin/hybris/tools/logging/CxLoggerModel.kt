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

package com.intellij.idea.plugin.hybris.tools.logging

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.ui.DeferredIcon
import com.intellij.util.asSafely
import javax.swing.Icon

data class CxLoggerModel(
    val name: String,
    val effectiveLevel: String,
    val parentName: String?,
    val inherited: Boolean,
    val icon: Icon,
    val level: LogLevel = LogLevel.of(effectiveLevel),
    val psiElementPointer: SmartPsiElementPointer<PsiElement>? = null
) {
    val resolved: Boolean
        get() = icon != HybrisIcons.Log.Identifier.NA

    companion object {

        const val ROOT_LOGGER_NAME = "root"

        fun of(
            name: String,
            effectiveLevel: String,
            parentName: String? = null,
            inherited: Boolean = false,
            icon: Icon? = null,
            psiElementPointer: SmartPsiElementPointer<PsiElement>? = null
        ): CxLoggerModel = CxLoggerModel(
            name = name,
            effectiveLevel = effectiveLevel,
            parentName = if (name == ROOT_LOGGER_NAME) null else parentName,
            inherited = inherited,
            icon = icon?.asSafely<DeferredIcon>()?.baseIcon ?: icon ?: HybrisIcons.Log.Identifier.NA,
            psiElementPointer = psiElementPointer
        )

        fun inherited(name: String, parentLogger: CxLoggerModel): CxLoggerModel = of(
            name = name,
            effectiveLevel = parentLogger.effectiveLevel,
            parentName = parentLogger.name,
            inherited = true,
        )

        fun rootFallback() = of(name = ROOT_LOGGER_NAME, effectiveLevel = "undefined")
    }
}