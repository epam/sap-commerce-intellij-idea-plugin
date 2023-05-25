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

package com.intellij.idea.plugin.hybris.impex.formatting;

import com.intellij.formatting.*;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 22:33 21 December 2014
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        return createModelInternally(formattingContext.getPsiElement(), formattingContext.getCodeStyleSettings());
    }

    @NotNull
    private FormattingModel createModelInternally(final PsiElement element, final CodeStyleSettings settings) {
        final Block impexBlock = new ImpexBlock(
            element.getNode(),
            null,
            Alignment.createAlignment(),
            createSpaceBuilder(settings),
            new ImpexUserRightsSpacingBuilder(settings),
            settings
        );

        return FormattingModelProvider.createFormattingModelForPsiFile(
            element.getContainingFile(),
            impexBlock,
            settings
        );
    }

    private static SpacingBuilder createSpaceBuilder(final CodeStyleSettings settings) {
        final ImpexCodeStyleSettings impexSettings = settings.getCustomSettings(ImpexCodeStyleSettings.class);

        return new SpacingBuilder(settings, ImpexLanguage.getInstance())
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

//                .before(ImpexTypes.LEFT_ROUND_BRACKET)
//                .spaceIf(impexSettings.SPACE_BEFORE_LEFT_ROUND_BRACKET)

//                .after(ImpexTypes.RIGHT_ROUND_BRACKET)
//                .spaceIf(impexSettings.SPACE_AFTER_RIGHT_ROUND_BRACKET)

            .before(ImpexTypes.RIGHT_ROUND_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_ROUND_BRACKET)

            .after(ImpexTypes.LEFT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_AFTER_LEFT_SQUARE_BRACKET)

//                .before(ImpexTypes.LEFT_SQUARE_BRACKET)
//                .spaceIf(impexSettings.SPACE_BEFORE_LEFT_SQUARE_BRACKET)

//                .after(ImpexTypes.RIGHT_SQUARE_BRACKET)
//                .spaceIf(impexSettings.SPACE_AFTER_RIGHT_SQUARE_BRACKET)

            .before(ImpexTypes.RIGHT_SQUARE_BRACKET)
            .spaceIf(impexSettings.SPACE_BEFORE_RIGHT_SQUARE_BRACKET)

            .after(ImpexTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_AFTER_ALTERNATIVE_PATTERN)

            .before(ImpexTypes.ALTERNATIVE_PATTERN)
            .spaceIf(impexSettings.SPACE_BEFORE_ALTERNATIVE_PATTERN)
            ;
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(final PsiFile file, final int offset, final ASTNode elementAtOffset) {
        return null;
    }
}
