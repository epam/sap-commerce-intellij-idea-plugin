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
package com.intellij.idea.plugin.hybris.impex.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ImpexVisitor extends PsiElementVisitor {

  public void visitAnyAttributeName(@NotNull ImpexAnyAttributeName o) {
    visitPsiElement(o);
  }

  public void visitAnyAttributeValue(@NotNull ImpexAnyAttributeValue o) {
    visitPsiElement(o);
  }

  public void visitAnyHeaderMode(@NotNull ImpexAnyHeaderMode o) {
    visitPsiElement(o);
  }

  public void visitAnyHeaderParameterName(@NotNull ImpexAnyHeaderParameterName o) {
    visitPsiElement(o);
  }

  public void visitAttribute(@NotNull ImpexAttribute o) {
    visitPsiElement(o);
  }

  public void visitBeanShell(@NotNull ImpexBeanShell o) {
    visitPsiElement(o);
  }

  public void visitComment(@NotNull ImpexComment o) {
    visitPsiElement(o);
  }

  public void visitFullHeaderParameter(@NotNull ImpexFullHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitFullHeaderType(@NotNull ImpexFullHeaderType o) {
    visitPsiElement(o);
  }

  public void visitHeaderLine(@NotNull ImpexHeaderLine o) {
    visitPsiElement(o);
  }

  public void visitHeaderTypeName(@NotNull ImpexHeaderTypeName o) {
    visitPsiElement(o);
  }

  public void visitMacroDeclaration(@NotNull ImpexMacroDeclaration o) {
    visitPsiElement(o);
  }

  public void visitMacroNameDec(@NotNull ImpexMacroNameDec o) {
    visitPsiNamedElement(o);
  }

  public void visitMacroUsageDec(@NotNull ImpexMacroUsageDec o) {
    visitPsiNamedElement(o);
  }

  public void visitMacroValueDec(@NotNull ImpexMacroValueDec o) {
    visitPsiElement(o);
  }

  public void visitModifiers(@NotNull ImpexModifiers o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull ImpexParameter o) {
    visitPsiElement(o);
  }

  public void visitParameters(@NotNull ImpexParameters o) {
    visitPsiElement(o);
  }

  public void visitRootMacroUsage(@NotNull ImpexRootMacroUsage o) {
    visitPsiElement(o);
  }

  public void visitString(@NotNull ImpexString o) {
    visitPsiElement(o);
  }

  public void visitSubParameters(@NotNull ImpexSubParameters o) {
    visitParameters(o);
  }

  public void visitSubTypeName(@NotNull ImpexSubTypeName o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull ImpexValue o) {
    visitPsiElement(o);
  }

  public void visitValueGroup(@NotNull ImpexValueGroup o) {
    visitPsiElement(o);
  }

  public void visitValueLine(@NotNull ImpexValueLine o) {
    visitPsiElement(o);
  }

  public void visitPsiNamedElement(@NotNull ImpexPsiNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
