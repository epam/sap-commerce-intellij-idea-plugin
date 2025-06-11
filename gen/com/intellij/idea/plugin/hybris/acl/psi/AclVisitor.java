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

  public void visitUserRightsEnd(@NotNull AclUserRightsEnd o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderLine(@NotNull AclUserRightsHeaderLine o) {
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

  public void visitUserRightsValueLine(@NotNull AclUserRightsValueLine o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueMemberOfGroups(@NotNull AclUserRightsValueMemberOfGroups o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValuePassword(@NotNull AclUserRightsValuePassword o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValuePermission(@NotNull AclUserRightsValuePermission o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueTarget(@NotNull AclUserRightsValueTarget o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueType(@NotNull AclUserRightsValueType o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueUid(@NotNull AclUserRightsValueUid o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderParameter(@NotNull AclUserRightsHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
