/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static sap.commerce.toolset.impex.psi.ImpExTypes.*;
import sap.commerce.toolset.impex.psi.*;
import java.util.Collection;

public class ImpExUserRightsHeaderParameterImpl extends ImpExUserRightsHeaderParameterMixin implements ImpExUserRightsHeaderParameter {

  public ImpExUserRightsHeaderParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitUserRightsHeaderParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

}
