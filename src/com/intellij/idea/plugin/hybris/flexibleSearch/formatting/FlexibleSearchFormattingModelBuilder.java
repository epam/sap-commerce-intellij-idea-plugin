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

package com.intellij.idea.plugin.hybris.flexibleSearch.formatting;


import com.intellij.formatting.*;
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.FROM;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.FROM_CLAUSE;
//import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.LEFT_BRACE;
//import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.LEFT_DOUBLE_BRACE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.ON;
//import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.RIGHT_BRACE;
//import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.RIGHT_DOUBLE_BRACE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.WHERE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.WHERE_CLAUSE;

public class FlexibleSearchFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        return createModelInternally(formattingContext.getPsiElement(), formattingContext.getCodeStyleSettings());
    }

    @NotNull
    private FormattingModel createModelInternally(
        final PsiElement element, final CodeStyleSettings settings
    ) {

        final FxSBlock block = new FxSBlock(
            element.getNode(),
            Alignment.createAlignment(),
            Indent.getNoneIndent(),
            Wrap.createWrap(WrapType.NONE, false),
            settings,
            createSpaceBuilder(settings)
        );
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), block, settings);
    }

    private static SpacingBuilder createSpaceBuilder(final CodeStyleSettings settings) {
        return new SpacingBuilder(settings, FlexibleSearchLanguage.Companion.getINSTANCE())
            .around(TokenSet.create(WHERE, FROM, ON))
            .spaces(1)
            .between(FROM_CLAUSE, WHERE_CLAUSE)
            .spaces(1)
            ;

//            .before(RIGHT_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_BRACES)
//            .after(LEFT_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_BRACES)
//
//            .before(RIGHT_DOUBLE_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)
//            .after(LEFT_DOUBLE_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(
        final PsiFile file, final int offset, final ASTNode elementAtOffset
    ) {
        return null;
    }
}
