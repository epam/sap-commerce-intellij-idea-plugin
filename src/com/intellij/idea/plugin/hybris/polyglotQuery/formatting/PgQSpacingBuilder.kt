/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.polyglotQuery.formatting

import com.intellij.formatting.SpacingBuilder
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet

class PgQSpacingBuilder(settings: CodeStyleSettings) : SpacingBuilder(settings, PolyglotQueryLanguage.instance) {

    init {
        this
            .before(RBRACE)
            .spaceIf(PgQCodeStyleSettings.SPACES_INSIDE_BRACES)

            .after(LBRACE)
            .spaceIf(PgQCodeStyleSettings.SPACES_INSIDE_BRACES)

            .before(
                TokenSet.create(
                    NULL_OPERATOR, NOT, NULL,
                    WHERE_CLAUSE, ORDER_BY,
                    AND, OR,
                )
            )
            .spaces(1)

            .after(
                TokenSet.create(
                    GET, WHERE, BY, COMMA, ORDER,
                    AND, OR,
                )
            )
            .spaces(1)

            .before(COMMA)
            .spaces(0)

            .around(TokenSet.create(CMP_OPERATOR))
            .spaceIf(PgQCodeStyleSettings.SPACE_AROUND_OP)

            .after(LBRACKET)
            .spaceIf(PgQCodeStyleSettings.SPACES_INSIDE_BRACKETS)
            .before(RBRACKET)
            .spaceIf(PgQCodeStyleSettings.SPACES_INSIDE_BRACKETS)
    }

}