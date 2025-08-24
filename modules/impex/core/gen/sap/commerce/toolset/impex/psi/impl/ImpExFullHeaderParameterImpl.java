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
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier;

public class ImpExFullHeaderParameterImpl extends ImpExFullHeaderParameterMixin implements ImpExFullHeaderParameter {

  public ImpExFullHeaderParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitFullHeaderParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpExAnyHeaderParameterName getAnyHeaderParameterName() {
    return findNotNullChildByClass(ImpExAnyHeaderParameterName.class);
  }

  @Override
  @NotNull
  public List<ImpExModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExModifiers.class);
  }

  @Override
  @NotNull
  public List<ImpExParameters> getParametersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExParameters.class);
  }

  @Override
  @Nullable
  public ImpExHeaderLine getHeaderLine() {
    return ImpExPsiUtil.getHeaderLine(this);
  }

  @Override
  @Nullable
  public ImpExAttribute getAttribute(@NotNull AttributeModifier attributeModifier) {
    return ImpExPsiUtil.getAttribute(this, attributeModifier);
  }

}
