/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.observable.util.addKeyListener
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ClientProperty
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import java.awt.event.KeyListener
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

fun <T> DialogWrapper.ifOk(onOk: () -> T): T? = if (this.showAndGet()) onOk()
else null

fun DialogWrapper.repackDialog() {
    invokeLater {
        peer.window?.pack()
    }
}

fun DialogWrapper.banner(
    text: String,
    status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Info
) = EditorNotificationPanel(null as FileEditor?, status).apply {
    this.text = if (text.contains("<html>")) text else "<html>$text</html>"
    border = JBUI.Borders.compound(
        ClientProperty.get(this, FileEditorManager.SEPARATOR_BORDER),
        border
    )
}

fun <T : KeyListener, J : JComponent> Cell<J>.addKeyListener(parentDisposable: Disposable? = null, listener: T): Cell<J> = this
    .apply { component.addKeyListener(parentDisposable, listener) }

fun JTree.addTreeSelectionListener(parentDisposable: Disposable? = null, listener: TreeSelectionListener): JTree = this
    .apply {
        addTreeSelectionListener(listener)
        parentDisposable?.whenDisposed { removeTreeSelectionListener(listener) }
    }

fun <T : TreeModelListener> JTree.addTreeModelListener(parentDisposable: Disposable? = null, listener: T): JTree = this
    .apply {
        model.addTreeModelListener(listener)
        parentDisposable?.whenDisposed { model.removeTreeModelListener(listener) }
    }

fun <T : MouseListener> JTree.addMouseListener(parentDisposable: Disposable? = null, listener: T): JTree = this
    .apply {
        addMouseListener(listener)
        parentDisposable?.whenDisposed { removeMouseListener(listener) }
    }

private fun Disposable.whenDisposed(onDispose: () -> Unit) {
    Disposer.register(this) { onDispose() }
}

fun <T : Any> TreePath.pathData(clazz: KClass<T>): T? = lastPathComponent
    .asSafely<DefaultMutableTreeNode>()
    ?.userObject
    ?.let { clazz.safeCast(it) }