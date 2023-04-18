// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*;

public class FlexibleSearchSelectStatementImpl extends ASTWrapperPsiElement implements FlexibleSearchSelectStatement {

  public FlexibleSearchSelectStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitSelectStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<FlexibleSearchCompoundOperator> getCompoundOperatorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchCompoundOperator.class);
  }

  @Override
  @Nullable
  public FlexibleSearchLimitClause getLimitClause() {
    return findChildByClass(FlexibleSearchLimitClause.class);
  }

  @Override
  @Nullable
  public FlexibleSearchOrderClause getOrderClause() {
    return findChildByClass(FlexibleSearchOrderClause.class);
  }

  @Override
  @NotNull
  public List<FlexibleSearchSelectCoreSelect> getSelectCoreSelectList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchSelectCoreSelect.class);
  }

}
