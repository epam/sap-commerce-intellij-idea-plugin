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
package com.intellij.idea.plugin.hybris.acl.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.idea.plugin.hybris.psi.FoldablePsiElement;

public class AclVisitor extends PsiElementVisitor {

  public void visitUserRights(@NotNull AclUserRights o) {
    visitFoldablePsiElement(o);
  }

  public void visitUserRightsBody(@NotNull AclUserRightsBody o) {
    visitFoldablePsiElement(o);
  }

  public void visitUserRightsEnd(@NotNull AclUserRightsEnd o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderLinePasswordAware(@NotNull AclUserRightsHeaderLinePasswordAware o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderLinePasswordUnaware(@NotNull AclUserRightsHeaderLinePasswordUnaware o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderParameterMemberOfGroups(@NotNull AclUserRightsHeaderParameterMemberOfGroups o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsHeaderParameterPassword(@NotNull AclUserRightsHeaderParameterPassword o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsHeaderParameterPermission(@NotNull AclUserRightsHeaderParameterPermission o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsHeaderParameterTarget(@NotNull AclUserRightsHeaderParameterTarget o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsHeaderParameterType(@NotNull AclUserRightsHeaderParameterType o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsHeaderParameterUid(@NotNull AclUserRightsHeaderParameterUid o) {
    visitUserRightsHeaderParameter(o);
  }

  public void visitUserRightsStart(@NotNull AclUserRightsStart o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueGroupBlank(@NotNull AclUserRightsValueGroupBlank o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupMemberOfGroups(@NotNull AclUserRightsValueGroupMemberOfGroups o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupPassword(@NotNull AclUserRightsValueGroupPassword o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupPermission(@NotNull AclUserRightsValueGroupPermission o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupTarget(@NotNull AclUserRightsValueGroupTarget o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupType(@NotNull AclUserRightsValueGroupType o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupUid(@NotNull AclUserRightsValueGroupUid o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroupUidBlank(@NotNull AclUserRightsValueGroupUidBlank o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueGroups(@NotNull AclUserRightsValueGroups o) {
    visitUserRightsValueGroup(o);
  }

  public void visitUserRightsValueLinePasswordAware(@NotNull AclUserRightsValueLinePasswordAware o) {
    visitUserRightsValueLine(o);
  }

  public void visitUserRightsValueLinePasswordUnaware(@NotNull AclUserRightsValueLinePasswordUnaware o) {
    visitUserRightsValueLine(o);
  }

  public void visitUserRightsValueLineTypePasswordAware(@NotNull AclUserRightsValueLineTypePasswordAware o) {
    visitUserRightsValueLine(o);
    // visitUserRightsValueLineType(o);
  }

  public void visitUserRightsValueLineTypePasswordUnaware(@NotNull AclUserRightsValueLineTypePasswordUnaware o) {
    visitUserRightsValueLine(o);
    // visitUserRightsValueLineType(o);
  }

  public void visitUserRightsValueLinesPasswordAware(@NotNull AclUserRightsValueLinesPasswordAware o) {
    visitFoldablePsiElement(o);
    // visitUserRightsValueLines(o);
  }

  public void visitUserRightsValueLinesPasswordUnaware(@NotNull AclUserRightsValueLinesPasswordUnaware o) {
    visitFoldablePsiElement(o);
    // visitUserRightsValueLines(o);
  }

  public void visitUserRightsValuePermission(@NotNull AclUserRightsValuePermission o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueTarget(@NotNull AclUserRightsValueTarget o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderParameter(@NotNull AclUserRightsHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueGroup(@NotNull AclUserRightsValueGroup o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueLine(@NotNull AclUserRightsValueLine o) {
    visitPsiElement(o);
  }

  public void visitFoldablePsiElement(@NotNull FoldablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
