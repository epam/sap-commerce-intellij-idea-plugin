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
package sap.commerce.toolset.impex.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ImpExVisitor extends PsiElementVisitor {

  public void visitAnyAttributeName(@NotNull ImpExAnyAttributeName o) {
    visitPsiElement(o);
  }

  public void visitAnyAttributeValue(@NotNull ImpExAnyAttributeValue o) {
    visitPsiElement(o);
  }

  public void visitAnyHeaderMode(@NotNull ImpExAnyHeaderMode o) {
    visitPsiElement(o);
  }

  public void visitAnyHeaderParameterName(@NotNull ImpExAnyHeaderParameterName o) {
    visitPsiElement(o);
  }

  public void visitAttribute(@NotNull ImpExAttribute o) {
    visitPsiElement(o);
  }

  public void visitBeanshellScriptBody(@NotNull ImpExBeanshellScriptBody o) {
    visitPsiElement(o);
  }

  public void visitComment(@NotNull ImpExComment o) {
    visitPsiElement(o);
  }

  public void visitDocumentIdDec(@NotNull ImpExDocumentIdDec o) {
    visitPsiNamedElement(o);
  }

  public void visitDocumentIdUsage(@NotNull ImpExDocumentIdUsage o) {
    visitPsiNamedElement(o);
  }

  public void visitFullHeaderParameter(@NotNull ImpExFullHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitFullHeaderType(@NotNull ImpExFullHeaderType o) {
    visitPsiElement(o);
  }

  public void visitGroovyScriptBody(@NotNull ImpExGroovyScriptBody o) {
    visitPsiElement(o);
  }

  public void visitHeaderLine(@NotNull ImpExHeaderLine o) {
    visitPsiElement(o);
  }

  public void visitHeaderTypeName(@NotNull ImpExHeaderTypeName o) {
    visitPsiElement(o);
  }

  public void visitJavascriptScriptBody(@NotNull ImpExJavascriptScriptBody o) {
    visitPsiElement(o);
  }

  public void visitMacroDeclaration(@NotNull ImpExMacroDeclaration o) {
    visitPsiElement(o);
  }

  public void visitMacroNameDec(@NotNull ImpExMacroNameDec o) {
    visitPsiNamedElement(o);
  }

  public void visitMacroUsageDec(@NotNull ImpExMacroUsageDec o) {
    visitPsiNamedElement(o);
  }

  public void visitMacroValueDec(@NotNull ImpExMacroValueDec o) {
    visitPsiElement(o);
  }

  public void visitModifiers(@NotNull ImpExModifiers o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull ImpExParameter o) {
    visitPsiElement(o);
  }

  public void visitParameters(@NotNull ImpExParameters o) {
    visitPsiElement(o);
  }

  public void visitRootMacroUsage(@NotNull ImpExRootMacroUsage o) {
    visitPsiElement(o);
  }

  public void visitScript(@NotNull ImpExScript o) {
    visitPsiElement(o);
  }

  public void visitString(@NotNull ImpExString o) {
    visitPsiElement(o);
  }

  public void visitSubParameters(@NotNull ImpExSubParameters o) {
    visitParameters(o);
  }

  public void visitSubTypeName(@NotNull ImpExSubTypeName o) {
    visitPsiElement(o);
  }

  public void visitUserRights(@NotNull ImpExUserRights o) {
    visitPsiElement(o);
  }

  public void visitUserRightsAttributeValue(@NotNull ImpExUserRightsAttributeValue o) {
    visitUserRightsValue(o);
  }

  public void visitUserRightsEnd(@NotNull ImpExUserRightsEnd o) {
    visitPsiElement(o);
  }

  public void visitUserRightsFirstValueGroup(@NotNull ImpExUserRightsFirstValueGroup o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderLine(@NotNull ImpExUserRightsHeaderLine o) {
    visitPsiElement(o);
  }

  public void visitUserRightsHeaderParameter(@NotNull ImpExUserRightsHeaderParameter o) {
    visitPsiElement(o);
  }

  public void visitUserRightsMultiValue(@NotNull ImpExUserRightsMultiValue o) {
    visitUserRightsValue(o);
  }

  public void visitUserRightsPermissionValue(@NotNull ImpExUserRightsPermissionValue o) {
    visitUserRightsValue(o);
  }

  public void visitUserRightsSingleValue(@NotNull ImpExUserRightsSingleValue o) {
    visitUserRightsValue(o);
  }

  public void visitUserRightsStart(@NotNull ImpExUserRightsStart o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueGroup(@NotNull ImpExUserRightsValueGroup o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValueLine(@NotNull ImpExUserRightsValueLine o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull ImpExValue o) {
    visitPsiElement(o);
  }

  public void visitValueGroup(@NotNull ImpExValueGroup o) {
    visitPsiElement(o);
  }

  public void visitValueLine(@NotNull ImpExValueLine o) {
    visitPsiElement(o);
  }

  public void visitPsiNamedElement(@NotNull ImpExPsiNamedElement o) {
    visitPsiElement(o);
  }

  public void visitUserRightsValue(@NotNull ImpExUserRightsValue o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
