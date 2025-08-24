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

public class ImpExParameterImpl extends ImpExParameterMixin implements ImpExParameter {

  public ImpExParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExDocumentIdUsage getDocumentIdUsage() {
    return findChildByClass(ImpExDocumentIdUsage.class);
  }

  @Override
  @NotNull
  public List<ImpExMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpExModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExModifiers.class);
  }

  @Override
  @Nullable
  public ImpExSubParameters getSubParameters() {
    return findChildByClass(ImpExSubParameters.class);
  }

  @Override
  @Nullable
  public String getReferenceItemTypeName() {
    return ImpExPsiUtil.getReferenceItemTypeName(this);
  }

  @Override
  @Nullable
  public String getReferenceName() {
    return ImpExPsiUtil.getReferenceName(this);
  }

  @Override
  @Nullable
  public String getItemTypeName() {
    return ImpExPsiUtil.getItemTypeName(this);
  }

  @Override
  @Nullable
  public String getInlineTypeName() {
    return ImpExPsiUtil.getInlineTypeName(this);
  }

  @Override
  @NotNull
  public String getAttributeName() {
    return ImpExPsiUtil.getAttributeName(this);
  }

}
