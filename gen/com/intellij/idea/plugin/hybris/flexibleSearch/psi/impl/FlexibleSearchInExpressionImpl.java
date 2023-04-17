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

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.LEFT_DOUBLE_BRACE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.RIGHT_DOUBLE_BRACE;

public class FlexibleSearchInExpressionImpl extends FlexibleSearchExpressionImpl implements FlexibleSearchInExpression {

  public FlexibleSearchInExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitInExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FlexibleSearchDefinedTableName getDefinedTableName() {
    return findChildByClass(FlexibleSearchDefinedTableName.class);
  }

  @Override
  @NotNull
  public List<FlexibleSearchExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchExpression.class);
  }

  @Override
  @Nullable
  public FlexibleSearchSelectStatement getSelectStatement() {
    return findChildByClass(FlexibleSearchSelectStatement.class);
  }

  @Override
  @Nullable
  public PsiElement getLeftDoubleBrace() {
    return findChildByType(LEFT_DOUBLE_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getRightDoubleBrace() {
    return findChildByType(RIGHT_DOUBLE_BRACE);
  }

}
