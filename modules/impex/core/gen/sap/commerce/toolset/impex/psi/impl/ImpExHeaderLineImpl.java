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
import com.intellij.openapi.util.TextRange;
import java.util.Collection;
import sap.commerce.toolset.impex.codeInspection.context.ImpExColumnContext;
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationContext;
import sap.commerce.toolset.psi.RangeAwareContent;

public class ImpExHeaderLineImpl extends ImpExHeaderLineMixin implements ImpExHeaderLine {

  public ImpExHeaderLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitHeaderLine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpExAnyHeaderMode getAnyHeaderMode() {
    return findNotNullChildByClass(ImpExAnyHeaderMode.class);
  }

  @Override
  @NotNull
  public List<ImpExFullHeaderParameter> getFullHeaderParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExFullHeaderParameter.class);
  }

  @Override
  @Nullable
  public ImpExFullHeaderType getFullHeaderType() {
    return findChildByClass(ImpExFullHeaderType.class);
  }

  @Override
  public @NotNull List<@NotNull ImpExDocumentIdDec> getDocumentIdDeclarations() {
    return ImpExPsiUtil.getDocumentIdDeclarations(this);
  }

  @Override
  public @NotNull List<@NotNull ImpExColumnContext> getColumnContexts() {
    return ImpExPsiUtil.getColumnContexts(this);
  }

}
