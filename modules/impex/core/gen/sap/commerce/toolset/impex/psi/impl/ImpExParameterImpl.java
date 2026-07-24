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

public class ImpExParameterImpl extends ImpExParameterMixin implements ImpExParameter {

  public ImpExParameterImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExDocumentIdUsage getDocumentIdUsage() {
    return findChildByClass(ImpExDocumentIdUsage.class);
  }

  @Override
  @NotNull
  public List<ImpExMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpExModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExModifiers.class);
  }

  @Override
  @NotNull
  public List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExPossibleMacroUsageDec.class);
  }

  @Override
  @Nullable
  public ImpExSubParameters getSubParameters() {
    return findChildByClass(ImpExSubParameters.class);
  }

  @Override
  public @Nullable String getReferenceItemTypeName() {
    return ImpExPsiUtil.getReferenceItemTypeName(this);
  }

  @Override
  public @Nullable String getReferenceName() {
    return ImpExPsiUtil.getReferenceName(this);
  }

  @Override
  public @Nullable String getItemTypeName() {
    return ImpExPsiUtil.getItemTypeName(this);
  }

  @Override
  public @Nullable String getInlineTypeName() {
    return ImpExPsiUtil.getInlineTypeName(this);
  }

  @Override
  public @NotNull String getAttributeName() {
    return ImpExPsiUtil.getAttributeName(this);
  }

  @Override
  public boolean isHeaderAbbreviation() {
    return ImpExPsiUtil.isHeaderAbbreviation(this);
  }

}
