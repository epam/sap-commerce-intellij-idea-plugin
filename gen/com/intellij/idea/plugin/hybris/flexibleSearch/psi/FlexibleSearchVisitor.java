/*
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

// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public class FlexibleSearchVisitor extends PsiElementVisitor {

  public void visitAndExpression(@NotNull FlexibleSearchAndExpression o) {
    visitExpression(o);
  }

  public void visitBetweenExpression(@NotNull FlexibleSearchBetweenExpression o) {
    visitExpression(o);
  }

  public void visitBindParameter(@NotNull FlexibleSearchBindParameter o) {
    visitPsiElement(o);
  }

  public void visitBitExpression(@NotNull FlexibleSearchBitExpression o) {
    visitExpression(o);
  }

  public void visitCaseExpression(@NotNull FlexibleSearchCaseExpression o) {
    visitExpression(o);
  }

  public void visitCastExpression(@NotNull FlexibleSearchCastExpression o) {
    visitExpression(o);
  }

  public void visitColumnAliasName(@NotNull FlexibleSearchColumnAliasName o) {
    visitPsiNamedElement(o);
  }

  public void visitColumnName(@NotNull FlexibleSearchColumnName o) {
    visitPsiElement(o);
  }

  public void visitColumnRefExpression(@NotNull FlexibleSearchColumnRefExpression o) {
    visitExpression(o);
  }

  public void visitComparisonExpression(@NotNull FlexibleSearchComparisonExpression o) {
    visitExpression(o);
  }

  public void visitCompoundOperator(@NotNull FlexibleSearchCompoundOperator o) {
    visitPsiElement(o);
  }

  public void visitConcatExpression(@NotNull FlexibleSearchConcatExpression o) {
    visitExpression(o);
  }

  public void visitDefinedTableName(@NotNull FlexibleSearchDefinedTableName o) {
    visitPsiElement(o);
  }

  public void visitEquivalenceExpression(@NotNull FlexibleSearchEquivalenceExpression o) {
    visitExpression(o);
  }

  public void visitExistsExpression(@NotNull FlexibleSearchExistsExpression o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull FlexibleSearchExpression o) {
    visitPsiElement(o);
  }

  public void visitFromClause(@NotNull FlexibleSearchFromClause o) {
    visitPsiElement(o);
  }

  public void visitFromTable(@NotNull FlexibleSearchFromTable o) {
    visitPsiElement(o);
  }

  public void visitGroupByClause(@NotNull FlexibleSearchGroupByClause o) {
    visitPsiElement(o);
  }

  public void visitInExpression(@NotNull FlexibleSearchInExpression o) {
    visitExpression(o);
  }

  public void visitIsnullExpression(@NotNull FlexibleSearchIsnullExpression o) {
    visitExpression(o);
  }

  public void visitJoinConstraint(@NotNull FlexibleSearchJoinConstraint o) {
    visitPsiElement(o);
  }

  public void visitJoinOperator(@NotNull FlexibleSearchJoinOperator o) {
    visitPsiElement(o);
  }

  public void visitLikeExpression(@NotNull FlexibleSearchLikeExpression o) {
    visitExpression(o);
  }

  public void visitLimitClause(@NotNull FlexibleSearchLimitClause o) {
    visitPsiElement(o);
  }

  public void visitLiteralExpression(@NotNull FlexibleSearchLiteralExpression o) {
    visitExpression(o);
  }

  public void visitMulExpression(@NotNull FlexibleSearchMulExpression o) {
    visitExpression(o);
  }

  public void visitOrExpression(@NotNull FlexibleSearchOrExpression o) {
    visitExpression(o);
  }

  public void visitOrderClause(@NotNull FlexibleSearchOrderClause o) {
    visitPsiElement(o);
  }

  public void visitOrderingTerm(@NotNull FlexibleSearchOrderingTerm o) {
    visitPsiElement(o);
  }

  public void visitParenExpression(@NotNull FlexibleSearchParenExpression o) {
    visitExpression(o);
  }

  public void visitResultColumn(@NotNull FlexibleSearchResultColumn o) {
    visitPsiElement(o);
  }

  public void visitResultColumns(@NotNull FlexibleSearchResultColumns o) {
    visitPsiElement(o);
  }

  public void visitSelectCore(@NotNull FlexibleSearchSelectCore o) {
    visitPsiElement(o);
  }

  public void visitSelectCoreSelect(@NotNull FlexibleSearchSelectCoreSelect o) {
    visitPsiElement(o);
  }

  public void visitSelectStatement(@NotNull FlexibleSearchSelectStatement o) {
    visitPsiElement(o);
  }

  public void visitSelectSubquery(@NotNull FlexibleSearchSelectSubquery o) {
    visitPsiElement(o);
  }

  public void visitSelectedTableName(@NotNull FlexibleSearchSelectedTableName o) {
    visitPsiElement(o);
  }

  public void visitSignedNumber(@NotNull FlexibleSearchSignedNumber o) {
    visitPsiElement(o);
  }

  public void visitTableAliasName(@NotNull FlexibleSearchTableAliasName o) {
    visitPsiNamedElement(o);
  }

  public void visitTableOrSubquery(@NotNull FlexibleSearchTableOrSubquery o) {
    visitPsiElement(o);
  }

  public void visitTypeName(@NotNull FlexibleSearchTypeName o) {
    visitPsiElement(o);
  }

  public void visitUnaryExpression(@NotNull FlexibleSearchUnaryExpression o) {
    visitExpression(o);
  }

  public void visitWhereClause(@NotNull FlexibleSearchWhereClause o) {
    visitPsiElement(o);
  }

  public void visitPsiNamedElement(@NotNull PsiNamedElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
