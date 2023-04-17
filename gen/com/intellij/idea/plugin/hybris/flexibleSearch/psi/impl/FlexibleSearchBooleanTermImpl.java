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

// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBooleanFactor;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBooleanTerm;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlexibleSearchBooleanTermImpl extends ASTWrapperPsiElement implements FlexibleSearchBooleanTerm {

    public FlexibleSearchBooleanTermImpl(@NotNull final ASTNode node) {
        super(node);
    }

    public void accept(@NotNull final FlexibleSearchVisitor visitor) {
        visitor.visitBooleanTerm(this);
    }

    @Override
    public void accept(@NotNull final PsiElementVisitor visitor) {
        if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @NotNull
    public FlexibleSearchBooleanFactor getBooleanFactor() {
        return findNotNullChildByClass(FlexibleSearchBooleanFactor.class);
    }

    @Override
    @NotNull
    public List<FlexibleSearchBooleanTerm> getBooleanTermList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchBooleanTerm.class);
    }

}
