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

public class ImpExAnyHeaderParameterNameImpl extends ImpExAnyHeaderParameterNameMixin implements ImpExAnyHeaderParameterName {

  public ImpExAnyHeaderParameterNameImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitAnyHeaderParameterName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ImpExDocumentIdDec getDocumentIdDec() {
    return findChildByClass(ImpExDocumentIdDec.class);
  }

  @Override
  @NotNull
  public List<ImpExMacroUsageDec> getMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExMacroUsageDec.class);
  }

  @Override
  @NotNull
  public List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExPossibleMacroUsageDec.class);
  }

  @Override
  @Nullable
  public ImpExSpecialParameter getSpecialParameter() {
    return findChildByClass(ImpExSpecialParameter.class);
  }

  @Override
  public @Nullable ImpExHeaderTypeName getHeaderItemTypeName() {
    return ImpExPsiUtil.getHeaderItemTypeName(this);
  }

  @Override
  public boolean isHeaderAbbreviation() {
    return ImpExPsiUtil.isHeaderAbbreviation(this);
  }

}
