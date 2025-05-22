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

    override fun documentation() = listOfNotNull(
        if (isInitial) "initial" else null,
        if (isOptional) "optional" else null,
        if (isRead) "read" else null,
        if (isWrite) "write" else null,
        if (isRemovable) "removable" else null,
        if (isPartOf) "partOf" else null,
        if (isUnique) "unique" else null,
        if (isPrivate) "private" else null,
        if (isSearch) "search" else null,
        if (isDoNotOptimize) "doNotOptimize" else null,
        if (isEncrypted) "encrypted" else null
    ).joinToString(",&nbsp;")

    override fun inlineDocumentation(): String {
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
            <table>
                <tr><td>I</td><td>initial</td></tr>
                <tr><td>O</td><td>optional</td></tr>
                <tr><td>R</td><td>read</td></tr>
                <tr><td>W</td><td>write</td></tr>
                <tr><td>S</td><td>search</td></tr>
                <tr><td>P</td><td>private</td></tr>
                <tr><td>D</td><td>removable</td></tr>
                <tr><td>pO</td><td>partOf</td></tr>
                <tr><td>U</td><td>unique</td></tr>
                <tr><td>dNO</td><td>doNotOptimize</td></tr>
                <tr><td>E</td><td>encrypted</td></tr>
            </table>
        """.trimIndent()
    }
}
