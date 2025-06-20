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
package com.intellij.idea.plugin.hybris.acl.formatting

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class AclCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings("AclCodeStyleSettings", container) {
    var SPACE_AFTER_FIELD_VALUE_SEPARATOR: Boolean = true
    var SPACE_BEFORE_FIELD_VALUE_SEPARATOR: Boolean = true

    var SPACE_AFTER_PARAMETERS_SEPARATOR: Boolean = true
    var SPACE_BEFORE_PARAMETERS_SEPARATOR: Boolean = false

    var SPACE_AFTER_COMMA: Boolean = true
    var SPACE_BEFORE_COMMA: Boolean = false
}
