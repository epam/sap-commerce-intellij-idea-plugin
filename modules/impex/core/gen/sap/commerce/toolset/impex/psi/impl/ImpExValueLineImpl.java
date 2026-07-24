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

public class ImpExValueLineImpl extends ImpExValueLineMixin implements ImpExValueLine {

  public ImpExValueLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitValueLine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExSubTypeName getSubTypeName() {
    return findChildByClass(ImpExSubTypeName.class);
  }

  @Override
  @NotNull
  public List<ImpExValueGroup> getValueGroupList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExValueGroup.class);
  }

  @Override
  public @Nullable ImpExValueGroup getValueGroup(int columnNumber) {
    return ImpExPsiUtil.getValueGroup(this, columnNumber);
  }

  @Override
  public void addValueGroups(int groupsToAdd) {
    ImpExPsiUtil.addValueGroups(this, groupsToAdd);
  }

}
