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
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.impex.psi.*;

public class ImpexUserRightsValueGroupImpl extends ASTWrapperPsiElement implements ImpexUserRightsValueGroup {

  public ImpexUserRightsValueGroupImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpexVisitor visitor) {
    visitor.visitUserRightsValueGroup(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpexUserRightsAttributeValue getUserRightsAttributeValue() {
    return findChildByClass(ImpexUserRightsAttributeValue.class);
  }

  @Override
  @Nullable
  public ImpexUserRightsMultiValue getUserRightsMultiValue() {
    return findChildByClass(ImpexUserRightsMultiValue.class);
  }

  @Override
  @Nullable
  public ImpexUserRightsPermissionValue getUserRightsPermissionValue() {
    return findChildByClass(ImpexUserRightsPermissionValue.class);
  }

  @Override
  @Nullable
  public ImpexUserRightsSingleValue getUserRightsSingleValue() {
    return findChildByClass(ImpexUserRightsSingleValue.class);
  }

  @Override
  @Nullable
  public ImpexUserRightsValueLine getValueLine() {
    return ImpexPsiUtil.getValueLine(this);
  }

  @Override
  @Nullable
  public Integer getColumnNumber() {
    return ImpexPsiUtil.getColumnNumber(this);
  }

  @Override
  @Nullable
  public ImpexUserRightsHeaderParameter getHeaderParameter() {
    return ImpexPsiUtil.getHeaderParameter(this);
  }

}
