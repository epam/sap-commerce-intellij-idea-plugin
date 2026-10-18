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

package sap.commerce.toolset.properties.exec

/**
 * Turns user input into the body of a single-quoted Groovy string literal.
 *
 * Anything other than a plain ASCII letter, digit or a few harmless punctuation marks is written as a `\uXXXX` escape.
 * That leaves no quote, backslash or line break which could end the literal early, and no `[...Placeholder]` token which
 * a later substitution into the same script would pick up.
 */
object CxGroovyStringLiteral {

    private const val SAFE_PUNCTUATION = " ._-:/@"

    fun escape(value: String) = buildString(value.length) {
        value.forEach { char ->
            if (char.isAsciiLetterOrDigit() || char in SAFE_PUNCTUATION) append(char)
            else append("\\u%04x".format(char.code))
        }
    }

    private fun Char.isAsciiLetterOrDigit() = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'
}
