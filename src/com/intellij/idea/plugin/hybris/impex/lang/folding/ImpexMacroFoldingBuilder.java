/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.lang.folding;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.impex.psi.*;
import com.intellij.idea.plugin.hybris.properties.PropertyService;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.SmartList;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfAnyType;

public class ImpexMacroFoldingBuilder implements FoldingBuilder {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final ASTNode node, @NotNull final Document document
    ) {
        if (!(node.getPsi() instanceof final ImpexFile root)) {
            return FoldingDescriptor.EMPTY_ARRAY;
        }

        final SmartList<FoldingDescriptor> results = new SmartList<>();
        final SmartList<ImpexMacroUsageDec> localMacroList = new SmartList<>();

        // resolve $config- first
        findChildrenOfAnyType(root, ImpexMacroUsageDec.class)
            .forEach(macroUsage -> resolveMacroUsage(macroUsage, results, localMacroList));

        // then resolve other macros
        root.acceptChildren(new ImpexVisitor() {
            @Override
            public void visitMacroDeclaration(@NotNull final ImpexMacroDeclaration macroDeclaration) {
                super.visitMacroDeclaration(macroDeclaration);
                resolveMacroDeclaration(macroDeclaration);
            }

            @Override
            public void visitString(@NotNull final ImpexString o) {
                super.visitString(o);
                resolveIncludeExternalData(o);
            }
        });

        // resolve local macro last
        localMacroList.forEach(macroUsage -> resolveLocalMacro(macroUsage, results));

        return results.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    private void resolveIncludeExternalData(final ImpexString impexString) {
        final String text = impexString.getText();
        int index = text.indexOf("impex.includeExternalData");
        if (index == -1) return;
        index = text.indexOf("getResourceAsStream");
        if (index == -1) return;
        final int startIndex = text.indexOf('(', index);
        if (startIndex == -1) return;
        final int endIndex = text.indexOf(')', startIndex);
        if (endIndex == -1) return;

        final var resource = StringUtils.strip(text.substring(startIndex + 1, endIndex), "\"' ");
        final var impexFile = (ImpexFile) impexString.getContainingFile();
        final var containingDirectory = impexFile.getContainingDirectory();

        if (containingDirectory == null) return;

        final var dirPath = containingDirectory.getVirtualFile().getCanonicalPath();
        final var referencedFile = LocalFileSystem.getInstance().findFileByIoFile(new File(dirPath, resource));
        if (referencedFile == null || !referencedFile.exists()) return;

        final var referencedPsi = PsiManager.getInstance(impexString.getProject()).findFile(referencedFile);
        if (!(referencedPsi instanceof final ImpexFile referencedImpExFile)) return;

        var referencedCache = referencedImpExFile.getMacroDescriptors();
        if (referencedCache.isEmpty()) {
            final var document = FileDocumentManager.getInstance().getDocument(referencedFile);
            if (document == null) return;

            preventRecursion(impexString, referencedCache);
            buildFoldRegions(referencedPsi.getNode(), document);
            referencedCache = referencedImpExFile.getMacroDescriptors();
        }
        impexFile.getMacroDescriptors().putAll(referencedCache);
    }

    private void preventRecursion(final ImpexString impexString, final MultiValuedMap<String, ImpexMacroDescriptor> cache) {
        if (cache.isEmpty()) {
            cache.put("!", new ImpexMacroDescriptor("!", "!", impexString));
        }
    }

    private void resolveMacroDeclaration(final ImpexMacroDeclaration macroLine) {
        final List<PsiElement> lineElements = getAllChildren(macroLine);
        String macroName = null;
        final StringBuilder sb = new StringBuilder();
        final var impexFile = (ImpexFile) macroLine.getContainingFile();
        final var cache = impexFile.getMacroDescriptors();
        PsiElement anchor = macroLine;
        for (PsiElement child : lineElements) {
            if (child instanceof final LeafPsiElement leafPsiElement) {
                if (leafPsiElement.getElementType() == ImpexTypes.ASSIGN_VALUE) {
                    continue;
                }
            }
            if (child instanceof ImpexMacroNameDec) {
                macroName = child.getText();
                anchor = child;
            } else {
                if (child instanceof final ImpexMacroUsageDec macroUsage) {
                    final ImpexMacroDescriptor descriptor = findInCache(impexFile, cache, macroUsage);
                    if (descriptor != null) {
                        sb.append(descriptor.resolvedValue());
                        final int delta = child.getText().length() - descriptor.macroName().length();
                        if (delta > 0) {
                            sb.append(child.getText().substring(descriptor.macroName().length()));
                        }
                    } else {
                        sb.append(child.getText());
                    }
                } else {
                    sb.append(child.getText());
                }
            }
        }

        cache.put(macroName, new ImpexMacroDescriptor(macroName, sb.toString().trim(), anchor));
    }

    private void resolveMacroUsage(
        final ImpexMacroUsageDec macroUsage,
        final SmartList<FoldingDescriptor> results,
        final SmartList<ImpexMacroUsageDec> localMacroList
    ) {
        final String text = macroUsage.getText();
        if (text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
            resolveProperty(macroUsage, results);
        } else {
            // local macro needs to be resolved later when evaluating macro declarations
            final PsiElement parent = macroUsage.getParent();
            if (parent instanceof ImpexMacroUsageDec) {
                return;
            }
            if (getRootPsi(parent) instanceof ImpexHeaderLine) {
                return;
            }
            localMacroList.add(macroUsage);
        }
    }

    private PsiElement getRootPsi(final PsiElement psiElement) {
        PsiElement root = psiElement;
        while (root.getParent() != null) {
            if (root.getParent() instanceof ImpexFile) {
                return root;
            }
            root = root.getParent();
        }
        return root;
    }

    private void resolveProperty(final ImpexMacroUsageDec macroUsage, final SmartList<FoldingDescriptor> results) {
        final String text = macroUsage.getText();
        if (text.length() <= HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length()) {
            return;
        }
        final var impexFile = (ImpexFile) macroUsage.getContainingFile();
        ImpexMacroDescriptor descriptor = impexFile.getSuitableMacroDescriptor(text, macroUsage);
        if (descriptor == null) {
            final var propertyName = text.replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "");
            final var propertyService = PropertyService.getInstance(macroUsage.getProject());

            if (propertyService == null) return;

            final var iProperty = propertyService.findMacroProperty(propertyName);

            if (iProperty == null) return;

            final var propertyKey = HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX + iProperty.getKey();

            descriptor = new ImpexMacroDescriptor(propertyKey, iProperty.getValue(), iProperty.getPsiElement());
            impexFile.getMacroDescriptors().put(text, descriptor);
        }
        final int start = macroUsage.getTextRange().getStartOffset();
        final TextRange range = new TextRange(start, start + descriptor.macroName().length());
        results.add(new FoldingDescriptor(macroUsage.getNode(), range, null));
    }

    private void resolveLocalMacro(final ImpexMacroUsageDec macroUsage, final SmartList<FoldingDescriptor> results) {
        final var impexFile = (ImpexFile) macroUsage.getContainingFile();
        final var cache = impexFile.getMacroDescriptors();
        String currentKey = "";
        final var macroUsageText = macroUsage.getText();
        for (final var key : cache.keySet()) {
            if (macroUsageText.startsWith(key)) {
                if (key.length() > currentKey.length()) {
                    currentKey = key;
                }
            }
        }
        if (currentKey.isEmpty()) return;

        final var descriptor = impexFile.getSuitableMacroDescriptor(currentKey, macroUsage);
        if (descriptor == null) return;

        final var start = macroUsage.getTextRange().getStartOffset();
        final var range = new TextRange(start, start + descriptor.macroName().length());

        results.add(new FoldingDescriptor(macroUsage.getNode(), range, null));

        cache.put(macroUsageText, descriptor);
    }

    @Nullable
    private ImpexMacroDescriptor findInCache(
        final ImpexFile impexFile,
        final MultiValuedMap<String, ImpexMacroDescriptor> cache,
        final ImpexMacroUsageDec macroUsageDec
    ) {
        final var macroName = macroUsageDec.getName();
        final var impexMacroDescriptor = impexFile.getSuitableMacroDescriptor(macroName, macroUsageDec);
        if (impexMacroDescriptor != null) return impexMacroDescriptor;

        for (final var md : cache.values()) {
            if (macroName.startsWith(md.macroName())) {
                cache.put(macroName, md);
                return md;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        final var psi = node.getPsi();
        final var descriptor = ((ImpexFile) psi.getContainingFile()).getSuitableMacroDescriptor(node.getText(), psi);

        if (descriptor == null) return node.getText();

        final var resolvedValue = descriptor.resolvedValue();

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

        return resolvedValue;
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

    private List<PsiElement> getAllChildren(final ImpexMacroDeclaration psiElement) {
        final List<PsiElement> result = new ArrayList<>();
        PsiElement psiChild = psiElement.getFirstChild();
        while (psiChild != null) {
            result.add(psiChild);
            psiChild = psiChild.getNextSibling();
        }
        return result;
    }

}
