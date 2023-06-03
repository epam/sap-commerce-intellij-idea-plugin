/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.formatting

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class ImpexCodeStyleSettings(settings: CodeStyleSettings) : CustomCodeStyleSettings("ImpexCodeStyleSettings", settings) {

    var SPACE_AFTER_FIELD_VALUE_SEPARATOR = true
    var SPACE_BEFORE_FIELD_VALUE_SEPARATOR = true

    var SPACE_AFTER_PARAMETERS_SEPARATOR = true
    var SPACE_BEFORE_PARAMETERS_SEPARATOR = false

    var SPACE_AFTER_COMMA = true
    var SPACE_BEFORE_COMMA = false

    var SPACE_AFTER_ATTRIBUTE_SEPARATOR = true
    var SPACE_BEFORE_ATTRIBUTE_SEPARATOR = false

    var SPACE_AFTER_FIELD_LIST_ITEM_SEPARATOR = true
    var SPACE_BEFORE_FIELD_LIST_ITEM_SEPARATOR = false

    var SPACE_AFTER_ASSIGN_VALUE = true
    var SPACE_BEFORE_ASSIGN_VALUE = true

    var SPACE_AFTER_LEFT_ROUND_BRACKET = false
    var SPACE_BEFORE_RIGHT_ROUND_BRACKET = false

    var SPACE_AFTER_LEFT_SQUARE_BRACKET = false
    var SPACE_BEFORE_RIGHT_SQUARE_BRACKET = false

    var SPACE_AFTER_ALTERNATIVE_PATTERN = true
    var SPACE_BEFORE_ALTERNATIVE_PATTERN = true

    var TABLIFY = true
}