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

public class FlexibleSearchFromClauseCombinedImpl extends ASTWrapperPsiElement implements FlexibleSearchFromClauseCombined {

  public FlexibleSearchFromClauseCombinedImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitFromClauseCombined(this);
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
  @NotNull
  public List<FlexibleSearchSelectSubqueryCombined> getSelectSubqueryCombinedList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchSelectSubqueryCombined.class);
  }

  @Override
  @Nullable
  public FlexibleSearchTableAliasName getTableAliasName() {
    return findChildByClass(FlexibleSearchTableAliasName.class);
  }

}
