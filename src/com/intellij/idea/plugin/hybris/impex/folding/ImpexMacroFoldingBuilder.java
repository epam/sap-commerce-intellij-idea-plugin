package com.intellij.idea.plugin.hybris.impex.folding;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.impex.utils.ProjectPropertiesUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.IProperty;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.SmartList;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfAnyType;

public class ImpexMacroFoldingBuilder implements FoldingBuilder {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(
        @NotNull final ASTNode node, @NotNull final Document document
    ) {
        if (!(node.getPsi() instanceof final ImpexFile root)) {
            return FoldingDescriptor.EMPTY;
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
                resolveMacroDeclaration(macroDeclaration, results);
            }

            @Override
            public void visitString(@NotNull final ImpexString o) {
                super.visitString(o);
                resolveIncludeExternalData(o);
            }
        });

        // resolve local macro last
        localMacroList.forEach(macroUsage->resolveLocalMacro(macroUsage, results));

        return results.toArray(FoldingDescriptor.EMPTY);
    }

    private void resolveIncludeExternalData(final ImpexString impexString) {
        final String text = impexString.getText();
        int index = text.indexOf("impex.includeExternalData");
        if (index == -1) {
            return;
        }
        index = text.indexOf("getResourceAsStream");
        if (index == -1) {
            return;
        }
        final int startIndex = text.indexOf('(', index);
        if (startIndex == -1) {
            return;
        }
        final int endIndex = text.indexOf(')', startIndex);
        if (endIndex == -1) {
            return;
        }
        final var resource = StringUtils.strip(text.substring(startIndex + 1, endIndex), "\"' ");
        final var directory = impexString.getContainingFile().getContainingDirectory().getVirtualFile();
        final var dirPath = directory.getCanonicalPath();
        final var referencedFile = LocalFileSystem.getInstance().findFileByIoFile(new File(dirPath, resource));
        if (referencedFile == null || !referencedFile.exists()) {
            return;
        }
        final PsiFile referencedPsi = PsiManager.getInstance(impexString.getProject()).findFile(referencedFile);
        if (!(referencedPsi instanceof ImpexFile)) {
            return;
        }
        ;
        Map<String, ImpexMacroDescriptor> referencedCache = ImpexMacroUtils.getFileCache(referencedPsi).getValue();
        if (referencedCache.isEmpty()) {
            final Document document = FileDocumentManager.getInstance().getDocument(referencedFile);
            if (document == null) {
                return;
            }
            preventRecursion(impexString);
            buildFoldRegions(referencedPsi.getNode(), document);
            referencedCache = ImpexMacroUtils.getFileCache(referencedPsi).getValue();
        }
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(impexString.getContainingFile()).getValue();
        cache.putAll(referencedCache);
    }

    private void preventRecursion(final ImpexString impexString) {
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(impexString.getContainingFile()).getValue();
        if (cache.isEmpty()) {
            cache.put("!", new ImpexMacroDescriptor("!", "!", impexString));
        }
    }

    private void resolveMacroDeclaration(
        final ImpexMacroDeclaration macroLine,
        final SmartList<FoldingDescriptor> results
    ) {
        final List<PsiElement> lineElements = getAllChildren(macroLine);
        String macroName = null;
        final StringBuilder sb = new StringBuilder();
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(macroLine.getContainingFile()).getValue();
        PsiElement anchor = macroLine;
        for (PsiElement child: lineElements) {
            if (child instanceof LeafPsiElement) {
                final LeafPsiElement leafPsiElement = (LeafPsiElement) child;
                if (leafPsiElement.getElementType() == ImpexTypes.ASSIGN_VALUE) {
                    continue;
                }
            }
            if (child instanceof ImpexMacroNameDec) {
                macroName = child.getText();
                anchor = child;
            } else {
                if (child instanceof ImpexMacroUsageDec) {
                    final ImpexMacroDescriptor descriptor = findInCache(cache, child.getText());
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
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(macroUsage.getContainingFile()).getValue();
        ImpexMacroDescriptor descriptor = cache.get(text);
        if (descriptor == null) {
            final String propertyName = text.substring(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length());
            final Module module = ModuleUtil.findModuleForPsiElement(macroUsage);
            if (module == null) {
                return;
            }
            final IProperty property = ProjectPropertiesUtils.INSTANCE.findMacroProperty(module, propertyName);
            if (property == null) {
                return;
            }
            final String value = ProjectPropertiesUtils.INSTANCE.resolvePropertyValue(module, property.getValue());
            descriptor = new ImpexMacroDescriptor(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX + property.getKey(), value, property.getPsiElement());
            cache.put(text, descriptor);
            cache.put(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX + property.getKey(), descriptor);
        }
        final int start = macroUsage.getTextRange().getStartOffset();
        final TextRange range = new TextRange(start, start + descriptor.macroName().length());
        results.add(new FoldingDescriptor(macroUsage.getNode(), range, null));
    }

    private void resolveLocalMacro(final ImpexMacroUsageDec macroUsage, final SmartList<FoldingDescriptor> results) {
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(macroUsage.getContainingFile()).getValue();
        String currentKey = "";
        for (String key: cache.keySet()) {
            if (macroUsage.getText().startsWith(key)) {
                if (key.length() > currentKey.length()) {
                    currentKey = key;
                }
            }
        }
        if (currentKey.isEmpty()) {
            return;
        }
        final ImpexMacroDescriptor descriptor = cache.get(currentKey);
        final int start = macroUsage.getTextRange().getStartOffset();
        final TextRange range = new TextRange(start, start + descriptor.macroName().length());
        results.add(new FoldingDescriptor(macroUsage.getNode(), range, null));

        cache.put(macroUsage.getText(), descriptor);
    }

    private ImpexMacroDescriptor findInCache(
        final Map<String, ImpexMacroDescriptor> cache,
        final String text
    ) {
        final ImpexMacroDescriptor impexMacroDescriptor = cache.get(text);
        if (impexMacroDescriptor != null) {
            return impexMacroDescriptor;
        }
        for (ImpexMacroDescriptor md: cache.values()) {
            if (text.startsWith(md.macroName())) {
                cache.put(text, md);
                return md;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull final ASTNode node) {
        final Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(node.getPsi().getContainingFile()).getValue();
        final ImpexMacroDescriptor descriptor = cache.get(node.getText());
        if (descriptor != null) {
            return descriptor.resolvedValue();
        }
        return node.getText();
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
