/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.editor.Editor
import com.intellij.pom.Navigatable
import com.intellij.util.OpenSourceUtil
import java.awt.Cursor
import javax.swing.Icon

abstract class AbstractSystemAwareInlayHintsCollector(editor: Editor) : FactoryInlayHintsCollector(editor) {

    protected val unknown: InlayPresentation by lazy {
        with(factory) {
            val icon = this.icon(HybrisIcons.CODE_NOT_GENERATED)
            val inset = this.inset(icon, right = 5, top = 3)
            this.withTooltip("Not yet generated", inset)
                .let { this.withCursorOnHover(it, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) }
        }
    }

    protected fun inlayPresentation(i: Icon, navigatables: Array<out Navigatable>, tooltipText: String = "Navigate to the Generated File") = with(factory) {
        val icon = this.icon(i)
        val inset = this.inset(icon, right = 5, top = 3)
        this.withTooltip(tooltipText, inset)
            .let { this.referenceOnHover(it) { _, _ -> OpenSourceUtil.navigate(*navigatables) } }
    }
}