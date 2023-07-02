/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.system.type.model.Modifiers

interface TSMetaModifiers : TSMetaClassifier<Modifiers> {

    val isRead: Boolean
    val isWrite: Boolean
    val isSearch: Boolean
    val isOptional: Boolean
    val isPrivate: Boolean
    val isInitial: Boolean
    val isRemovable: Boolean
    val isPartOf: Boolean
    val isUnique: Boolean
    val isDoNotOptimize: Boolean
    val isEncrypted: Boolean

    fun inlineName(): String {
        val meta = this
        return buildString {
            if (meta.isInitial) append("I")
            if (meta.isOptional) append("O")
            if (meta.isRead) append("R")
            if (meta.isWrite) append("W")
            if (meta.isSearch) append("S")
            if (meta.isPrivate) append("P")
            if (meta.isRemovable) append("D")
            if (meta.isPartOf) append("pO")
            if (meta.isUnique) append("U")
            if (meta.isDoNotOptimize) append("dNO")
            if (meta.isEncrypted) append("E")
        }
    }

    companion object {

        val tableHeaderTooltip = """
            <strong>Modifiers</strong>
            <ul>
                <li>I - initial</li>
                <li>O - optional</li>
                <li>R - read</li>
                <li>W - write</li>
                <li>S - search</li>
                <li>P - private</li>
                <li>D - removable</li>
                <li>pO - partOf</li>
                <li>U - unique</li>
                <li>dNO - doNotOptimize</li>
                <li>E - encrypted</li>
            </ul>
        """.trimIndent()
    }
}
