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
package com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*;

public class FlexibleSearchColumnRefYExpressionImpl extends FlexibleSearchExpressionImpl implements FlexibleSearchColumnRefYExpression {

  public FlexibleSearchColumnRefYExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitColumnRefYExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FlexibleSearchColumnLocalizedName getColumnLocalizedName() {
    return findChildByClass(FlexibleSearchColumnLocalizedName.class);
  }

  @Override
  @NotNull
  public FlexibleSearchColumnName getColumnName() {
    return findNotNullChildByClass(FlexibleSearchColumnName.class);
  }

  @Override
  @Nullable
  public FlexibleSearchColumnOuterJoinName getColumnOuterJoinName() {
    return findChildByClass(FlexibleSearchColumnOuterJoinName.class);
  }

  @Override
  @Nullable
  public FlexibleSearchColumnSeparator getColumnSeparator() {
    return findChildByClass(FlexibleSearchColumnSeparator.class);
  }

  @Override
  @Nullable
  public FlexibleSearchSelectedTableName getSelectedTableName() {
    return findChildByClass(FlexibleSearchSelectedTableName.class);
  }

}
