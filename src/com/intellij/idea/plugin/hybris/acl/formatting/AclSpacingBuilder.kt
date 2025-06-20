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

import com.intellij.formatting.SpacingBuilder
import com.intellij.idea.plugin.hybris.acl.AclLanguage
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet

class AclSpacingBuilder(
    settings: CodeStyleSettings,
    aclSettings: AclCodeStyleSettings
) : SpacingBuilder(settings, AclLanguage) {

    init {
        this
            .before(AclTypes.PARAMETERS_SEPARATOR)
            .spaceIf(aclSettings.SPACE_BEFORE_PARAMETERS_SEPARATOR)

            .after(AclTypes.PARAMETERS_SEPARATOR)
            .spaceIf(aclSettings.SPACE_AFTER_PARAMETERS_SEPARATOR)

            .after(AclTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(aclSettings.SPACE_AFTER_FIELD_VALUE_SEPARATOR)

            .before(AclTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(aclSettings.SPACE_BEFORE_FIELD_VALUE_SEPARATOR)

            .after(AclTypes.COMMA)
            .spaceIf(aclSettings.SPACE_AFTER_COMMA)

            .before(AclTypes.COMMA)
            .spaceIf(aclSettings.SPACE_BEFORE_COMMA)

            .around(AclTypes.DOT)
            .spaces(0)

            .around(TokenSet.create(
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_TYPE,
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_UID,
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_PASSWORD,
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS,
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_TARGET,
                AclTypes.USER_RIGHTS_HEADER_PARAMETER_PERMISSION,

                AclTypes.USER_RIGHTS_VALUE_GROUP_TYPE,
                AclTypes.USER_RIGHTS_VALUE_GROUP_UID,
                AclTypes.USER_RIGHTS_VALUE_GROUP_PASSWORD,
                AclTypes.USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS,
                AclTypes.USER_RIGHTS_VALUE_GROUP_TARGET,
                AclTypes.USER_RIGHTS_VALUE_GROUP_PERMISSION,
            ))
            .spaces(0)

            .before(TokenSet.create(
                AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE,
                AclTypes.USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE
            ))
            .spaces(0)
    }
}
