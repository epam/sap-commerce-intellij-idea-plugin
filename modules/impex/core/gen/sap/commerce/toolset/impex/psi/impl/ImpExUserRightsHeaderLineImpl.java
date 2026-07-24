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

public class ImpExUserRightsHeaderLineImpl extends ASTWrapperPsiElement implements ImpExUserRightsHeaderLine {

  public ImpExUserRightsHeaderLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitUserRightsHeaderLine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ImpExUserRightsHeaderParameter> getUserRightsHeaderParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExUserRightsHeaderParameter.class);
  }

  @Override
  public @Nullable ImpExUserRightsHeaderParameter getHeaderParameter(int index) {
    return ImpExPsiUtil.getHeaderParameter(this, index);
  }

}
