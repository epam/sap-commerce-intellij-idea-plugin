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
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes.*;
import com.intellij.idea.plugin.hybris.impex.psi.*;

public class ImpexParameterImpl extends ImpexParameterMixin implements ImpexParameter {

  public ImpexParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ImpexMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpexModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpexModifiers.class);
  }

  @Override
  @Nullable
  public ImpexSubParameters getSubParameters() {
    return findChildByClass(ImpexSubParameters.class);
  }

  @Override
  @Nullable
  public String getReferenceItemTypeName() {
    return ImpexPsiUtil.getReferenceItemTypeName(this);
  }

  @Override
  @Nullable
  public String getReferenceName() {
    return ImpexPsiUtil.getReferenceName(this);
  }

  @Override
  @Nullable
  public String getItemTypeName() {
    return ImpexPsiUtil.getItemTypeName(this);
  }

  @Override
  @Nullable
  public String getInlineTypeName() {
    return ImpexPsiUtil.getInlineTypeName(this);
  }

  @Override
  @NotNull
  public String getAttributeName() {
    return ImpexPsiUtil.getAttributeName(this);
  }

}
