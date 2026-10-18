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

package sap.commerce.toolset.ui.event

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

interface DocumentListener : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) = Unit
    override fun removeUpdate(e: DocumentEvent) = Unit
    override fun changedUpdate(e: DocumentEvent) = Unit
}

/**
 * A [DocumentListener] which reacts to every kind of document mutation in the same way —
 * the common case for "re-filter as the user types" text fields.
 */
fun documentListener(onChange: () -> Unit) = object : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) = onChange()
    override fun removeUpdate(e: DocumentEvent) = onChange()
    override fun changedUpdate(e: DocumentEvent) = onChange()
}
