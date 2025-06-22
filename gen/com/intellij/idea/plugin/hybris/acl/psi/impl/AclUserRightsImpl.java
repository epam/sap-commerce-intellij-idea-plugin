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
package com.intellij.idea.plugin.hybris.acl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.acl.psi.AclTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.acl.psi.*;

public class AclUserRightsImpl extends ASTWrapperPsiElement implements AclUserRights {

  public AclUserRightsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AclVisitor visitor) {
    visitor.visitUserRights(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AclVisitor) accept((AclVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AclUserRightsBody getUserRightsBody() {
    return findChildByClass(AclUserRightsBody.class);
  }

  @Override
  @Nullable
  public AclUserRightsEnd getUserRightsEnd() {
    return findChildByClass(AclUserRightsEnd.class);
  }

  @Override
  @NotNull
  public AclUserRightsStart getUserRightsStart() {
    return findNotNullChildByClass(AclUserRightsStart.class);
  }

}
