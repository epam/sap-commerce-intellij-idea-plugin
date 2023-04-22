/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 *
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
package com.intellij.idea.plugin.hybris.polyglotQuery.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.*;

public class PolyglotQueryExprAtomImpl extends ASTWrapperPsiElement implements PolyglotQueryExprAtom {

  public PolyglotQueryExprAtomImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PolyglotQueryVisitor visitor) {
    visitor.visitExprAtom(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PolyglotQueryVisitor) accept((PolyglotQueryVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PolyglotQueryAttributeKey getAttributeKey() {
    return findChildByClass(PolyglotQueryAttributeKey.class);
  }

  @Override
  @Nullable
  public PolyglotQueryCmpOperator getCmpOperator() {
    return findChildByClass(PolyglotQueryCmpOperator.class);
  }

  @Override
  @Nullable
  public PolyglotQueryExprOr getExprOr() {
    return findChildByClass(PolyglotQueryExprOr.class);
  }

  @Override
  @Nullable
  public PolyglotQueryNullOperator getNullOperator() {
    return findChildByClass(PolyglotQueryNullOperator.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
