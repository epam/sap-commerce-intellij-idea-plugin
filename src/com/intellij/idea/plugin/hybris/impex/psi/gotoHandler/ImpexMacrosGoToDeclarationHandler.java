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

package com.intellij.idea.plugin.hybris.impex.psi.gotoHandler;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexMacroDescriptor;
import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexMacroUtils;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


/**
 * Class is <b>workaround</b> because of goto declaration functional not working via PsiReferenceContributor, when
 * {@code ImpexTypes.MACRO_USAGE} place on root impex file.
 *
 * @author Aleksandr Nosov <nosovae.dev@gmail.com>
 */
public class ImpexMacrosGoToDeclarationHandler extends GotoDeclarationHandlerBase {

    @Nullable
    @Override
    public PsiElement getGotoDeclarationTarget(final PsiElement sourceElement, final Editor editor) {
        if (!(sourceElement instanceof LeafPsiElement)
            || !(((LeafPsiElement) sourceElement)
                     .getElementType().equals(ImpexTypes.MACRO_USAGE))) {
            return null;
        }

        final PsiFile originalFile = sourceElement.getContainingFile();

        Map<String, ImpexMacroDescriptor> cache = ImpexMacroUtils.getFileCache(originalFile).getValue();
        ImpexMacroDescriptor descriptor = cache.get(sourceElement.getText());
        if (descriptor != null) {
            return descriptor.psiElement();
        }
        return null;
    }
}