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
package com.intellij.idea.plugin.hybris.polyglotQuery.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.idea.plugin.hybris.psi.FoldablePsiElement;

public class PolyglotQueryVisitor extends PsiElementVisitor {

  public void visitAttributeKey(@NotNull PolyglotQueryAttributeKey o) {
    visitFoldablePsiElement(o);
  }

  public void visitBindParameter(@NotNull PolyglotQueryBindParameter o) {
    visitPsiElement(o);
  }

  public void visitCmpOperator(@NotNull PolyglotQueryCmpOperator o) {
    visitPsiElement(o);
  }

  public void visitExprAnd(@NotNull PolyglotQueryExprAnd o) {
    visitPsiElement(o);
  }

  public void visitExprAtom(@NotNull PolyglotQueryExprAtom o) {
    visitPsiElement(o);
  }

  public void visitExprOr(@NotNull PolyglotQueryExprOr o) {
    visitPsiElement(o);
  }

  public void visitLocalizedName(@NotNull PolyglotQueryLocalizedName o) {
    visitPsiElement(o);
  }

  public void visitNullOperator(@NotNull PolyglotQueryNullOperator o) {
    visitPsiElement(o);
  }

  public void visitOrderBy(@NotNull PolyglotQueryOrderBy o) {
    visitFoldablePsiElement(o);
  }

  public void visitOrderKey(@NotNull PolyglotQueryOrderKey o) {
    visitPsiElement(o);
  }

  public void visitQuery(@NotNull PolyglotQueryQuery o) {
    visitPsiElement(o);
  }

  public void visitTypeKey(@NotNull PolyglotQueryTypeKey o) {
    visitFoldablePsiElement(o);
  }

  public void visitWhereClause(@NotNull PolyglotQueryWhereClause o) {
    visitFoldablePsiElement(o);
  }

  public void visitFoldablePsiElement(@NotNull FoldablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
