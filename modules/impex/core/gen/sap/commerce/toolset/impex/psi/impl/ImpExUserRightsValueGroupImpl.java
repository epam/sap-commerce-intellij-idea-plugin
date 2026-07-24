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
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import sap.commerce.toolset.impex.psi.*;

public class ImpExUserRightsValueGroupImpl extends ASTWrapperPsiElement implements ImpExUserRightsValueGroup {

  public ImpExUserRightsValueGroupImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitUserRightsValueGroup(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExUserRightsAttributeValue getUserRightsAttributeValue() {
    return findChildByClass(ImpExUserRightsAttributeValue.class);
  }

  @Override
  @Nullable
  public ImpExUserRightsMultiValue getUserRightsMultiValue() {
    return findChildByClass(ImpExUserRightsMultiValue.class);
  }

  @Override
  @Nullable
  public ImpExUserRightsPermissionValue getUserRightsPermissionValue() {
    return findChildByClass(ImpExUserRightsPermissionValue.class);
  }

  @Override
  @Nullable
  public ImpExUserRightsSingleValue getUserRightsSingleValue() {
    return findChildByClass(ImpExUserRightsSingleValue.class);
  }

  @Override
  public @Nullable ImpExUserRightsValueLine getValueLine() {
    return ImpExPsiUtil.getValueLine(this);
  }

  @Override
  public @Nullable Integer getColumnNumber() {
    return ImpExPsiUtil.getColumnNumber(this);
  }

  @Override
  public @Nullable ImpExUserRightsHeaderParameter getHeaderParameter() {
    return ImpExPsiUtil.getHeaderParameter(this);
  }

}
