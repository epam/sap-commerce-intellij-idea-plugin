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
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import java.awt.Color
import java.awt.Font
import java.awt.event.ItemListener
import java.awt.event.KeyListener
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.ScrollPaneConstants
import javax.swing.border.Border
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

fun scrollPanel(content: JComponent, horizontalScrollBarPolicy: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) = panel {
    row {
        scrollCell(content)
            .align(Align.Companion.FILL)
            .resizableColumn()
            .applyToComponent {
                (this.parent.parent as? JBScrollPane)?.apply {
                    this.horizontalScrollBarPolicy = horizontalScrollBarPolicy
                    border = JBEmptyBorder(0)
                }
            }

    }.resizableRow()
}

internal fun <T : Any> TreePath.pathData(clazz: KClass<T>): T? = lastPathComponent
    .asSafely<DefaultMutableTreeNode>()
    ?.userObject
    ?.let { clazz.safeCast(it) }

internal fun <J : JComponent> Cell<J>.border(border: Border?): Cell<J> = this.apply { component.border = border }
internal fun <J : JComponent> Cell<J>.background(background: Color?): Cell<J> = this.apply { component.background = background }
internal fun <J : JComponent> Cell<J>.opaque(opaque: Boolean): Cell<J> = this.apply { component.isOpaque = opaque }
internal fun <J : JComponent> Cell<J>.font(font: Font): Cell<J> = this.apply { component.font = font }
internal fun <J : Any> Cell<ComboBox<J>>.addItemListener(parentDisposable: Disposable? = null, listener: ItemListener): Cell<ComboBox<J>> = this
    .apply { component.addItemListener(parentDisposable, listener) }

internal fun <T : KeyListener, J : JComponent> Cell<J>.addKeyListener(parentDisposable: Disposable? = null, listener: T): Cell<J> = this
    .apply { component.addKeyListener(parentDisposable, listener) }

internal fun JTree.addTreeSelectionListener(parentDisposable: Disposable? = null, listener: TreeSelectionListener): JTree = this
    .apply {
        addTreeSelectionListener(listener)
        parentDisposable?.whenDisposed { removeTreeSelectionListener(listener) }
    }

internal fun <T : TreeModelListener> JTree.addTreeModelListener(parentDisposable: Disposable? = null, listener: T): JTree = this
    .apply {
        model.addTreeModelListener(listener)
        parentDisposable?.whenDisposed { model.removeTreeModelListener(listener) }
    }

internal fun <T : MouseListener> JTree.addMouseListener(parentDisposable: Disposable? = null, listener: T): JTree = this
    .apply {
        addMouseListener(listener)
        parentDisposable?.whenDisposed { removeMouseListener(listener) }
    }

private fun Disposable.whenDisposed(onDispose: () -> Unit) {
    Disposer.register(this) { onDispose() }
}
