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

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

class ImpexFormattingModelBuilder : FormattingModelBuilder {

    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
        val impexSettings = settings.getCustomSettings(ImpexCodeStyleSettings::class.java)

        return SpacingBuilder(settings, ImpexLanguage.getInstance())
            .before(ImpexTypes.VALUE_GROUP)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_VALUE_SEPARATOR)

            .after(ImpexTypes.FIELD_VALUE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_FIELD_VALUE_SEPARATOR)

            .before(ImpexTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_PARAMETERS_SEPARATOR)

            .after(ImpexTypes.PARAMETERS_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_PARAMETERS_SEPARATOR)

            .before(ImpexTypes.ATTRIBUTE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_ATTRIBUTE_SEPARATOR)

            .after(ImpexTypes.COMMA)
            .spaceIf(impexSettings.SPACE_AFTER_COMMA)

            .before(ImpexTypes.COMMA)
            .spaceIf(impexSettings.SPACE_BEFORE_COMMA)

            .after(ImpexTypes.ATTRIBUTE_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_ATTRIBUTE_SEPARATOR)

            .before(ImpexTypes.FIELD_LIST_ITEM_SEPARATOR)
            .spaceIf(impexSettings.SPACE_BEFORE_FIELD_LIST_ITEM_SEPARATOR)

            .after(ImpexTypes.FIELD_LIST_ITEM_SEPARATOR)
            .spaceIf(impexSettings.SPACE_AFTER_FIELD_LIST_ITEM_SEPARATOR)

            .after(ImpexTypes.ASSIGN_VALUE)
            .spaceIf(impexSettings.SPACE_AFTER_ASSIGN_VALUE)

            .before(ImpexTypes.ASSIGN_VALUE)
            .spaceIf(impexSettings.SPACE_BEFORE_ASSIGN_VALUE)

            .after(ImpexTypes.LEFT_ROUND_BRACKET)
            .spaceIf(impexSettings.SPACE_AFTER_LEFT_ROUND_BRACKET)

            .before(ImpexTypes.RIGHT_ROUND_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_ROUND_BRACKET)

            .after(ImpexTypes.LEFT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_AFTER_LEFT_SQUARE_BRACKET)

            .before(ImpexTypes.RIGHT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_SQUARE_BRACKET)

            .after(ImpexTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_AFTER_ALTERNATIVE_PATTERN)

            .before(ImpexTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_BEFORE_ALTERNATIVE_PATTERN)
    }

    override fun createModel(formattingContext: FormattingContext) = createModelInternally(
        formattingContext.psiElement, formattingContext.codeStyleSettings)

    private fun createModelInternally(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        val impexBlock = ImpexBlock(
            node =  element.node,
            alignment = Alignment.createAlignment(),
            spacingBuilder = createSpaceBuilder(settings),
            codeStyleSettings = settings,
            alignmentStrategy = getAlignmentStrategy(settings)
        )

        return FormattingModelProvider.createFormattingModelForPsiFile(element.containingFile, impexBlock, settings)
    }

    private fun getAlignmentStrategy(settings: CodeStyleSettings): AlignmentStrategy {
        val impexCodeStyleSettings = settings.getCustomSettings(ImpexCodeStyleSettings::class.java)

        return if (impexCodeStyleSettings.TABLIFY) TableAlignmentStrategy()
        else ColumnsAlignmentStrategy()
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode?) = null
}
