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

package com.intellij.idea.plugin.hybris.impex.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

class ImpexUserRightsSpacingBuilder(
    settings: CodeStyleSettings
) : SpacingBuilder(settings, ImpexLanguage.getInstance()) {

    private val impexSettings: ImpexCodeStyleSettings = settings.getCustomSettings(ImpexCodeStyleSettings::class.java)

    init {
        this
            .before(ImpexTypes.VALUE_GROUP)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_VALUE_SEPARATOR)

            .after(ImpexTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_FIELD_VALUE_SEPARATOR)

            .before(ImpexTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_VALUE_SEPARATOR)

            .before(ImpexTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_PARAMETERS_SEPARATOR)

            .after(ImpexTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_PARAMETERS_SEPARATOR)

            .after(ImpexTypes.COMMA)
            .spaceIf(impexSettings.SPACE_AFTER_COMMA)

            .before(ImpexTypes.COMMA)
            .spaceIf(impexSettings.SPACE_BEFORE_COMMA)

            .before(TokenSet.create(ImpexTypes.USER_RIGHTS_HEADER_LINE, ImpexTypes.USER_RIGHTS_VALUE_LINE))
            .spaces(0)

            .before(ImpexTypes.DOT)
            .spaces(0)

            .after(ImpexTypes.DOT)
            .spaces(0)
    }

    override fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
        val childNode1 = (child1 as? ASTBlock)?.node
            ?: return super.getSpacing(null, null, child2)
        val childNode2 = (child2 as? ASTBlock)?.node
            ?: return super.getSpacing(null, null, child2)

        // separator between "JOIN <table>"
//        if (child1.node?.elementType == FlexibleSearchTypes.JOIN_OPERATOR && child2.node?.elementType == FlexibleSearchTypes.TABLE_OR_SUBQUERY) {
//            val spaces = longestJoinOperatorSpaces(childNode2) - (child1.node?.textLength ?: 0)
//            return Spacing.createSpacing(spaces, spaces, 0, true, 0)
//        } else if (child1.node?.elementType == FlexibleSearchTypes.TABLE_OR_SUBQUERY && child2.node?.elementType == FlexibleSearchTypes.JOIN_CONSTRAINT) {
//            val longestOffset = longestTableAliasName(childNode2)
//            val currentJoinConstraintLength = PsiTreeUtil.findChildOfType(child1.node?.psi, FlexibleSearchTableAliasName::class.java)?.textLength ?: 0
//
//            val spaces = longestOffset - currentJoinConstraintLength + 1
//            return Spacing.createSpacing(spaces, spaces, 0, true, 0)
//        }

        return super.getSpacing(parent, child1, child2)
    }

}