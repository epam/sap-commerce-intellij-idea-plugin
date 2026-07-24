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

public class ImpExUserRightsValueLineImpl extends ASTWrapperPsiElement implements ImpExUserRightsValueLine {

  public ImpExUserRightsValueLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitUserRightsValueLine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExUserRightsFirstValueGroup getUserRightsFirstValueGroup() {
    return findChildByClass(ImpExUserRightsFirstValueGroup.class);
  }

  @Override
  @NotNull
  public List<ImpExUserRightsValueGroup> getUserRightsValueGroupList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExUserRightsValueGroup.class);
  }

  @Override
  public @Nullable ImpExUserRightsValueGroup getValueGroup(int index) {
    return ImpExPsiUtil.getValueGroup(this, index);
  }

  @Override
  public @Nullable ImpExUserRightsHeaderLine getHeaderLine() {
    return ImpExPsiUtil.getHeaderLine(this);
  }

}
