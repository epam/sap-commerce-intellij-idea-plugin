/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.flexibleSearch

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.lexer.FlexAdapter

class FlexibleSearchLexer : FlexAdapter(_FlexibleSearchLexer()) {
    companion object {
        fun needsQuoting(name: String): Boolean {
            val lexer = FlexibleSearchLexer()
            lexer.start(name)
            return lexer.tokenType != FlexibleSearchTypes.IDENTIFIER || lexer.tokenEnd != lexer.bufferEnd
        }

        /** Checks if the given name (table name, column name) needs escaping and returns a string that's safe to put in SQL. */
        @JvmStatic
        fun getValidName(name: String): String {
            return if (!needsQuoting(name)) name else "`${name.replace("`", "``")}`"
        }

        /**
         * Checks if the given string value needs escaping and returns a string that's safe to put in SQL as a string value.
         */
        @JvmStatic
        fun getValidStringValue(name: String): String {
            // We can't use the back tick character (`) for strings because it's not a valid character to create strings
            return if (!needsQuoting(name)) "'$name'" else "'${name.replace("'", "''")}'"
        }
    }
}