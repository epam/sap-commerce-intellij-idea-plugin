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

package com.intellij.idea.plugin.hybris.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.util.addItemListener
import com.intellij.openapi.observable.util.addKeyListener
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBEmptyBorder
import java.awt.Color
import java.awt.Font
import java.awt.event.ItemListener
import java.awt.event.KeyListener
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants
import javax.swing.border.Border

object Dsl {

    fun scrollPanel(content: JComponent, horizontalScrollBarPolicy: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) = panel {
        row {
            scrollCell(content)
                .align(Align.FILL)
                .resizableColumn()
                .applyToComponent {
                    (this.parent.parent as? JBScrollPane)?.apply {
                        this.horizontalScrollBarPolicy = horizontalScrollBarPolicy
                        border = JBEmptyBorder(0)
                    }
                }

        }.resizableRow()
    }

    internal fun <J : JComponent> Cell<J>.border(border: Border?): Cell<J> = this.apply { component.border = border }
    internal fun <J : JComponent> Cell<J>.background(background: Color?): Cell<J> = this.apply { component.background = background }
    internal fun <J : JComponent> Cell<J>.opaque(opaque: Boolean): Cell<J> = this.apply { component.isOpaque = opaque }
    internal fun <J : JComponent> Cell<J>.font(font: Font): Cell<J> = this.apply { component.font = font }

    internal fun <J : Any> Cell<ComboBox<J>>.addItemListener(parentDisposable: Disposable? = null, listener: ItemListener): Cell<ComboBox<J>> = this.apply { component.addItemListener(parentDisposable, listener) }

    internal fun <J : JComponent> Cell<J>.addKeyListener(parentDisposable: Disposable? = null, listener: KeyListener): Cell<J> = this.apply { component.addKeyListener(parentDisposable, listener) }
}