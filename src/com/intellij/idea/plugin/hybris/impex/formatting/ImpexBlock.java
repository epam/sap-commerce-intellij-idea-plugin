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
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
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
    private final CodeStyleSettings codeStyleSettings;
    private final AlignmentStrategy alignmentStrategy;

    public ImpexBlock(
        @NotNull final ASTNode node,
        @Nullable final Wrap wrap,
        @Nullable final Alignment alignment,
        @NotNull final SpacingBuilder spacingBuilder,
        @NotNull final CodeStyleSettings codeStyleSettings,
        @NotNull final AlignmentStrategy alignmentStrategy) {
        super(node, wrap, alignment);

        this.spacingBuilder = spacingBuilder;
        this.codeStyleSettings = codeStyleSettings;
        this.alignmentStrategy = alignmentStrategy;
    }

    @Override
    protected List<Block> buildChildren() {
        final List<Block> blocks = new ArrayList<Block>();

        alignmentStrategy.processNode(myNode);

        ASTNode currentNode = myNode.getFirstChildNode();

        while (null != currentNode) {
            alignmentStrategy.processNode(currentNode);

            if (isNotWhitespaceOrNewLine(currentNode) && !isCurrentNodeHasParentValue(currentNode)) {
                final var block = new ImpexBlock(
                    currentNode,
                    null,
                    alignmentStrategy.getAlignment(currentNode),
                    spacingBuilder,
                    codeStyleSettings,
                    alignmentStrategy
                );

                blocks.add(block);
            }

            currentNode = currentNode.getTreeNext();
        }

        return blocks;
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
