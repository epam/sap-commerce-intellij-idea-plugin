package com.intellij.idea.plugin.hybris.impex.folding;

import com.intellij.psi.PsiElement;

public record ImpexMacroDescriptor(String macroName, String resolvedValue, PsiElement psiElement) {

    public ImpexMacroDescriptor(final String macroName, final String resolvedValue, final PsiElement psiElement) {
        this.psiElement = psiElement;
        this.macroName = macroName;
        this.resolvedValue = resolvedValue == null || resolvedValue.isEmpty()
            ? "<blank>"
            : resolvedValue;
    }
}
