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

package sap.commerce.toolset.editor

import com.intellij.openapi.actionSystem.AnAction

interface ResultsSplitEditor : SplitEditor {

    /**
     * Whether the in-editor results panel is currently visible.
     *
     * Setting this to `false` hides the panel without discarding its content,
     * so it can be made visible again by setting it back to `true`.
     */
    var inEditorResults: Boolean

    /**
     * Title shown as a bold label on the left side of the in-editor results panel action bar.
     *
     * Each language-specific split editor provides a descriptive heading
     * (e.g. "ImpEx Execution Results").
     */
    val inEditorResultsTitle: String

    /**
     * Returns additional [AnAction]s contributed by the concrete editor to the
     * in-editor results panel action bar.
     *
     * Actions are rendered on the **left** side of the bar, before the shared
     * built-in controls (e.g. the Hide button on the right).
     *
     * Override in a language-specific split editor to expose result-related
     * actions (export, copy, filter, …) directly inside the panel.
     * The default implementation returns an empty list.
     */
    fun inEditorResultsActions(): List<AnAction> = emptyList()
}
