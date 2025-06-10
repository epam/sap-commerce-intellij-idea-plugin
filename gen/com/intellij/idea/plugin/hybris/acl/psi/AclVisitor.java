/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 *
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.acl.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class AclVisitor extends PsiElementVisitor {

  public void visitComment(@NotNull AclComment o) {
    visitPsiElement(o);
  }

  public void visitUserRights(@NotNull AclUserRights o) {
    visitPsiElement(o);
  }

  public void visitUserRightsAttributeValue(@NotNull AclUserRightsAttributeValue o) {
    visitPsiElement(o);
  }

  public void visitUserRightsEnd(@NotNull AclUserRightsEnd o) {
    visitPsiElement(o);
  }

  public void visitUserRightsFirstValueGroup(@NotNull AclUserRightsFirstValueGroup o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderLine(@NotNull AclUserRightsHeaderLine o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderParameter(@NotNull AclUserRightsHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitUserRightsMultiValue(@NotNull AclUserRightsMultiValue o) {
    visitPsiElement(o);
  }

  public void visitUserRightsPermissionValue(@NotNull AclUserRightsPermissionValue o) {
    visitPsiElement(o);
  }

  public void visitUserRightsSingleValue(@NotNull AclUserRightsSingleValue o) {
    visitPsiElement(o);
  }

  public void visitUserRightsStart(@NotNull AclUserRightsStart o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueGroup(@NotNull AclUserRightsValueGroup o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueLine(@NotNull AclUserRightsValueLine o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
