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

package sap.commerce.toolset.impex.lang.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.impex.ImpExConstants;
import sap.commerce.toolset.impex.psi.ImpExFile;
import sap.commerce.toolset.impex.psi.ImpExHeaderLine;
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec;
import sap.commerce.toolset.impex.settings.ImpExFoldingSettings;

import java.util.HashSet;
import java.util.Objects;

public class ImpExMacroFoldingBuilder implements FoldingBuilder {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final ASTNode node, @NotNull final Document document
    ) {
        if (!(node.getPsi() instanceof final ImpExFile root)) {
            return FoldingDescriptor.EMPTY_ARRAY;
        }

        final var foldingSettings = ImpExFoldingSettings.getInstance();

        if (!foldingSettings.getEnabled()) return FoldingDescriptor.EMPTY_ARRAY;

        final var foldMacroInParameters = foldingSettings
            .getFoldMacroInParameters();

        final var macroUsages = PsiTreeUtil.findChildrenOfAnyType(root, ImpExMacroUsageDec.class).stream()
            .map(it -> acceptMacroUsage(it, foldMacroInParameters))
            .filter(Objects::nonNull)
            .toList();

        return macroUsages.stream()
            .map(this::buildFoldRegion)
            .filter(Objects::nonNull)
            .toArray(FoldingDescriptor[]::new);
    }

    @Nullable
    private ImpExMacroUsageDec acceptMacroUsage(final ImpExMacroUsageDec macroUsage, final boolean foldMacroInParameters) {
        final var text = macroUsage.getText();

        // local macro needs to be resolved later when evaluating macro declarations
        final var parent = macroUsage.getParent();
        if (parent instanceof ImpExMacroUsageDec) return null;
        if (!foldMacroInParameters && getRootPsi(parent) instanceof ImpExHeaderLine) return null;

        return macroUsage;
    }

    private PsiElement getRootPsi(final PsiElement psiElement) {
        PsiElement root = psiElement;
        while (root.getParent() != null) {
            if (root.getParent() instanceof ImpExFile) {
                return root;
            }
            root = root.getParent();
        }
        return root;
    }

    @Nullable
    private FoldingDescriptor buildFoldRegion(final ImpExMacroUsageDec macroUsage) {
        final var configPropertyMacro = macroUsage.getText().startsWith(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX);

        if (configPropertyMacro && macroUsage.getText().length() == ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length()) return null;

        final var reference = macroUsage.getReference();
        if (reference == null) return null;

        final var start = macroUsage.getTextRange().getStartOffset();
        final var end = configPropertyMacro
            ? start + reference.getCanonicalText().length() + ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length()
            : start + reference.getCanonicalText().length();
        final var range = new TextRange(start, end);

        return new FoldingDescriptor(macroUsage.getNode(), range, null);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        final var psi = node.getPsi();

        if (!(psi instanceof final ImpExMacroUsageDec impexMacroUsageDec)) return node.getText();

        final var resolvedValue = impexMacroUsageDec.resolveValue(new HashSet<>());

        if (resolvedValue.startsWith("jar:")) {
            final var blocks = resolvedValue.substring("jar:".length()).split("&");
            if (blocks.length == 2) {
                final var loaderClass = blocks[0];
                return "jar:"
                    + loaderClass.substring(loaderClass.lastIndexOf('.') + 1)
                    + '&'
                    + getFileName(blocks[1]);
            }
        } else if (resolvedValue.startsWith("zip:")) {
            final var blocks = resolvedValue.substring("zip:".length()).split("&");
            if (blocks.length == 2) {
                final var zipName = getFileName(blocks[0]);
                return "zip:" + zipName + '&' + blocks[1];
            }
        } else if (resolvedValue.startsWith("file:")) {
            final var blocks = resolvedValue.split(":");
            if (blocks.length == 2) {
                final var fileName = getFileName(blocks[1]);
                return "file:" + fileName;
            }
        }

        return StringUtils.defaultIfBlank(resolvedValue, " ");
    }

    @NotNull
    private static String getFileName(final String fileName) {
        var name = fileName;

        if (StringUtils.countMatches(name, '\\') <= 1 && StringUtils.countMatches(name, '/') <= 1) {
            return name;
        }

        final var backslashIndex = name.lastIndexOf('\\');
        if (backslashIndex >= 0) name = name.substring(backslashIndex);
        final var slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) name = name.substring(slashIndex);
        return ".." + name;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        return true;
    }

}
