/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameters;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.idea.plugin.hybris.impex.psi.references.FunctionTSAttributeReference;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexDocumentIdReference;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacrosReferenceBase;
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSAttributeReference;
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public abstract class ImpexAnyHeaderParameterNameMixin extends ASTWrapperPsiElement implements
                                                                                    ImpexAnyHeaderParameterName {

    private ImpexTSAttributeReference myReference;

    public ImpexAnyHeaderParameterNameMixin(@NotNull final ASTNode astNode) {
        super(astNode);
    }

    @Override
    public void subtreeChanged() {
        putUserData(ImpexTSAttributeReference.getCACHE_KEY(), null);

        final var header = PsiTreeUtil.getParentOfType(this, ImpexFullHeaderParameter.class);
        if (header != null) {
            header.getParametersList().stream()
                  .map(ImpexParameters::getParameterList)
                  .flatMap(Collection::stream)
                  .forEach(it -> it.putUserData(FunctionTSAttributeReference.getCACHE_KEY(), null));
        }
    }

    @Override
    public PsiReference getReference() {
        final PsiReference[] references = getReferences();
        return references.length > 0
            ? references[0]
            : null;
    }

    @NotNull
    @Override
    public final PsiReference[] getReferences() {
        final IElementType leafType = Optional.ofNullable(getFirstChild())
                                              .map(PsiElement::getNode)
                                              .map(ASTNode::getElementType)
                                              .orElse(null);

        if (ImpexTypes.MACRO_USAGE.equals(leafType)) {
            return new PsiReference[]{new ImpexMacrosReferenceBase(this)};
        }

        if (ImpexTypes.DOCUMENT_ID.equals(leafType)) {
            return new PsiReference[]{new ImpexDocumentIdReference(this)};
        }

        //optimisation: don't even try for macro's and documents
        if (!ImpexTypes.HEADER_PARAMETER_NAME.equals(leafType) &&
            !ImpexTypes.FUNCTION.equals(leafType)) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (PsiUtils.shouldCreateNewReference(myReference, getText())) {
            myReference = new ImpexTSAttributeReference(this);
        }
        return new PsiReference[]{myReference};
    }

    @Override
    protected Object clone() {
        final ImpexAnyHeaderParameterNameMixin result = (ImpexAnyHeaderParameterNameMixin) super.clone();
        result.myReference = null;
        return result;
    }
}
