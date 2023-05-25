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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRights;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsAwarePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created 22:21 21 December 2014
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexBlock extends AbstractBlock {

    private final SpacingBuilder spacingBuilder;
    private final ImpexUserRightsSpacingBuilder userRightsSpacingBuilder;
    private final CodeStyleSettings codeStyleSettings;

    public ImpexBlock(
        @NotNull final ASTNode node,
        @Nullable final Wrap wrap,
        @Nullable final Alignment alignment,
        @NotNull final SpacingBuilder spacingBuilder,
        @NotNull final ImpexUserRightsSpacingBuilder userRightsSpacingBuilder,
        @NotNull final CodeStyleSettings codeStyleSettings
    ) {
        super(node, wrap, alignment);

        this.spacingBuilder = spacingBuilder;
        this.userRightsSpacingBuilder = userRightsSpacingBuilder;
        this.codeStyleSettings = codeStyleSettings;
    }

    @Override
    protected List<Block> buildChildren() {
        final List<Block> blocks = new ArrayList<Block>();

        final AlignmentStrategy alignmentStrategy = getAlignmentStrategy();
        alignmentStrategy.processNode(myNode);

        ASTNode currentNode = myNode.getFirstChildNode();

        while (null != currentNode) {
            final boolean isNotWhiteSpace = isNotWhitespaceOrNewLine(currentNode);
            final boolean isUserRightsBlock;
            if (isNotWhiteSpace &&
                (currentNode.getPsi() instanceof ImpexUserRightsAwarePsiElement
                    || currentNode.getPsi() instanceof ImpexUserRights
                    || PsiTreeUtil.getParentOfType(currentNode.getPsi(), ImpexUserRights.class) != null
                )) {
                final var block = new ImpexUserRightsBlock(
                    currentNode,
                    null,
                    Indent.getNoneIndent(),
                    Wrap.createWrap(WrapType.NONE, false),
                    codeStyleSettings,
                    userRightsSpacingBuilder
                );

                blocks.add(block);
                isUserRightsBlock = true;
            } else {
                isUserRightsBlock = false;
            }

            if (!isUserRightsBlock) {
                alignmentStrategy.processNode(currentNode);

                if (isNotWhiteSpace && !isCurrentNodeHasParentValue(currentNode)) {
                    final var block = new ImpexBlock(
                        currentNode,
                        null,
                        alignmentStrategy.getAlignment(currentNode),
                        spacingBuilder,
                        userRightsSpacingBuilder,
                        codeStyleSettings
                    );

                    blocks.add(block);
                }
            }

            currentNode = currentNode.getTreeNext();
        }

        return blocks;
    }

    @NotNull
    private AlignmentStrategy getAlignmentStrategy() {
        final ImpexCodeStyleSettings impexCodeStyleSettings = this.codeStyleSettings.getCustomSettings(
            ImpexCodeStyleSettings.class
        );

        if (impexCodeStyleSettings.TABLIFY) {

            return ApplicationManager.getApplication().getService(TableAlignmentStrategy.class);
        }

        return ApplicationManager.getApplication().getService(ColumnsAlignmentStrategy.class);
    }

    private boolean isNotWhitespaceOrNewLine(final ASTNode currentNode) {
        return TokenType.WHITE_SPACE != currentNode.getElementType()
               && ImpexTypes.CRLF != currentNode.getElementType();
    }

    private boolean isCurrentNodeHasParentValue(final @NotNull ASTNode currentNode) {
        return Objects.equals(currentNode.getTreeParent().getElementType(), ImpexTypes.VALUE);
    }

    @Override
    public Indent getIndent() {
        return Indent.getNoneIndent();
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable final Block child1, @NotNull final Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }
}
