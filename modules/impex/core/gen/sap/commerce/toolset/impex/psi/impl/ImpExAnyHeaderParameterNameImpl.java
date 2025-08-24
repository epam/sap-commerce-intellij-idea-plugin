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

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import sap.commerce.toolset.impex.psi.*;

public class ImpExAnyHeaderParameterNameImpl extends ImpExAnyHeaderParameterNameMixin implements ImpExAnyHeaderParameterName {

  public ImpExAnyHeaderParameterNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitAnyHeaderParameterName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExDocumentIdDec getDocumentIdDec() {
    return findChildByClass(ImpExDocumentIdDec.class);
  }

  @Override
  @Nullable
  public ImpExMacroUsageDec getMacroUsageDec() {
    return findChildByClass(ImpExMacroUsageDec.class);
  }

  @Override
  @Nullable
  public ImpExHeaderTypeName getHeaderItemTypeName() {
    return ImpExPsiUtil.getHeaderItemTypeName(this);
  }

}
