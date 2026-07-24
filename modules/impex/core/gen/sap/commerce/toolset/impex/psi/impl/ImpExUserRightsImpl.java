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

public class ImpExUserRightsImpl extends ImpExUserRightsMixin implements ImpExUserRights {

  public ImpExUserRightsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitUserRights(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExUserRightsEnd getUserRightsEnd() {
    return findChildByClass(ImpExUserRightsEnd.class);
  }

  @Override
  @Nullable
  public ImpExUserRightsHeaderLine getUserRightsHeaderLine() {
    return findChildByClass(ImpExUserRightsHeaderLine.class);
  }

  @Override
  @NotNull
  public ImpExUserRightsStart getUserRightsStart() {
    return findNotNullChildByClass(ImpExUserRightsStart.class);
  }

  @Override
  @NotNull
  public List<ImpExUserRightsValueLine> getUserRightsValueLineList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExUserRightsValueLine.class);
  }

}
