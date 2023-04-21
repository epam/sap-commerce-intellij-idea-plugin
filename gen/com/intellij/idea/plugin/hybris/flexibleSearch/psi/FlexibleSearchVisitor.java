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
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.idea.plugin.hybris.psi.FoldablePsiElement;

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
    visitPsiElement(o);
  }

  public void visitColumnLocalizedName(@NotNull FlexibleSearchColumnLocalizedName o) {
    visitPsiElement(o);
  }

  public void visitColumnName(@NotNull FlexibleSearchColumnName o) {
    visitPsiElement(o);
  }

  public void visitColumnOuterJoinName(@NotNull FlexibleSearchColumnOuterJoinName o) {
    visitPsiElement(o);
  }

  public void visitColumnRefExpression(@NotNull FlexibleSearchColumnRefExpression o) {
    visitExpression(o);
    // visitFoldablePsiElement(o);
  }

  public void visitColumnRefYExpression(@NotNull FlexibleSearchColumnRefYExpression o) {
    visitExpression(o);
    // visitFoldablePsiElement(o);
  }

  public void visitColumnSeparator(@NotNull FlexibleSearchColumnSeparator o) {
    visitPsiElement(o);
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

  public void visitExtParameterName(@NotNull FlexibleSearchExtParameterName o) {
    visitPsiElement(o);
  }

  public void visitFromClause(@NotNull FlexibleSearchFromClause o) {
    visitPsiElement(o);
  }

  public void visitFromClauseExpression(@NotNull FlexibleSearchFromClauseExpression o) {
    visitExpression(o);
  }

  public void visitFromClauseSelect(@NotNull FlexibleSearchFromClauseSelect o) {
    visitPsiElement(o);
  }

  public void visitFromClauseSelectQuery(@NotNull FlexibleSearchFromClauseSelectQuery o) {
    visitPsiElement(o);
  }

  public void visitFromClauseSimple(@NotNull FlexibleSearchFromClauseSimple o) {
    visitPsiElement(o);
  }

  public void visitFromClauseSubqueries(@NotNull FlexibleSearchFromClauseSubqueries o) {
    visitPsiElement(o);
  }

  public void visitFromTable(@NotNull FlexibleSearchFromTable o) {
    visitFoldablePsiElement(o);
  }

  public void visitFunctionCallExpression(@NotNull FlexibleSearchFunctionCallExpression o) {
    visitExpression(o);
  }

  public void visitFunctionName(@NotNull FlexibleSearchFunctionName o) {
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

  public void visitMysqlFunctionExpression(@NotNull FlexibleSearchMysqlFunctionExpression o) {
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

  public void visitSelectCoreSelect(@NotNull FlexibleSearchSelectCoreSelect o) {
    visitFoldablePsiElement(o);
  }

  public void visitSelectStatement(@NotNull FlexibleSearchSelectStatement o) {
    visitPsiElement(o);
  }

  public void visitSelectSubquery(@NotNull FlexibleSearchSelectSubquery o) {
    visitPsiElement(o);
  }

  public void visitSelectSubqueryCombined(@NotNull FlexibleSearchSelectSubqueryCombined o) {
    visitPsiElement(o);
  }

  public void visitSelectedTableName(@NotNull FlexibleSearchSelectedTableName o) {
    visitPsiNamedElement(o);
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

  public void visitYFromClause(@NotNull FlexibleSearchYFromClause o) {
    visitPsiElement(o);
  }

  public void visitPsiNamedElement(@NotNull FlexibleSearchPsiNamedElement o) {
    visitPsiElement(o);
  }

  public void visitFoldablePsiElement(@NotNull FoldablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
