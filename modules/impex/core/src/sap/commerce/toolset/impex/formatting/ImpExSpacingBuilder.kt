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

package sap.commerce.toolset.impex.formatting

import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.psi.ImpExTypes

class ImpExSpacingBuilder(
    settings: CodeStyleSettings,
    impexSettings: ImpExCodeStyleSettings
) : SpacingBuilder(settings, ImpExLanguage) {

    init {
        this
            .between(
                ImpExTypes.ANY_HEADER_MODE,
                ImpExTypes.FULL_HEADER_TYPE
            )
            .spaces(1)

            .before(ImpExTypes.VALUE_GROUP)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_VALUE_SEPARATOR)

            .after(ImpExTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_FIELD_VALUE_SEPARATOR)

            .before(ImpExTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_PARAMETERS_SEPARATOR)

            .after(ImpExTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_PARAMETERS_SEPARATOR)

            .before(ImpExTypes.ATTRIBUTE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_ATTRIBUTE_SEPARATOR)

            .after(ImpExTypes.COMMA)
            .spaceIf(impexSettings.SPACE_AFTER_COMMA)

            .before(ImpExTypes.COMMA)
            .spaceIf(impexSettings.SPACE_BEFORE_COMMA)

            .after(ImpExTypes.ATTRIBUTE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_ATTRIBUTE_SEPARATOR)

            .before(ImpExTypes.FIELD_LIST_ITEM_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_LIST_ITEM_SEPARATOR)

            .after(ImpExTypes.FIELD_LIST_ITEM_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_FIELD_LIST_ITEM_SEPARATOR)

            .after(ImpExTypes.ASSIGN_VALUE)
            .spaceIf(impexSettings.SPACE_AFTER_ASSIGN_VALUE)

            .before(ImpExTypes.ASSIGN_VALUE)
            .spaceIf(impexSettings.SPACE_BEFORE_ASSIGN_VALUE)

            .after(ImpExTypes.LEFT_ROUND_BRACKET)
            .spaceIf(impexSettings.SPACE_AFTER_LEFT_ROUND_BRACKET)

            .before(ImpExTypes.RIGHT_ROUND_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_ROUND_BRACKET)

            .after(ImpExTypes.LEFT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_AFTER_LEFT_SQUARE_BRACKET)

            .before(ImpExTypes.RIGHT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_SQUARE_BRACKET)

            .after(ImpExTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_AFTER_ALTERNATIVE_PATTERN)

            .before(ImpExTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_BEFORE_ALTERNATIVE_PATTERN)
    }

}