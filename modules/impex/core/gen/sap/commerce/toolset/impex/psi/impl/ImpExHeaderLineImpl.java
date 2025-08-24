/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import sap.commerce.toolset.impex.psi.*;
import com.intellij.openapi.util.TextRange;

public class ImpExHeaderLineImpl extends ImpExHeaderLineMixin implements ImpExHeaderLine {

  public ImpExHeaderLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitHeaderLine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpExAnyHeaderMode getAnyHeaderMode() {
    return findNotNullChildByClass(ImpExAnyHeaderMode.class);
  }

  @Override
  @NotNull
  public List<ImpExFullHeaderParameter> getFullHeaderParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExFullHeaderParameter.class);
  }

  @Override
  @Nullable
  public ImpExFullHeaderType getFullHeaderType() {
    return findChildByClass(ImpExFullHeaderType.class);
  }

  @Override
  @NotNull
  public TextRange getTableRange() {
    return ImpExPsiUtil.getTableRange(this);
  }

  @Override
  @NotNull
  public List<ImpExFullHeaderParameter> getUniqueFullHeaderParameters() {
    return ImpExPsiUtil.getUniqueFullHeaderParameters(this);
  }

}
