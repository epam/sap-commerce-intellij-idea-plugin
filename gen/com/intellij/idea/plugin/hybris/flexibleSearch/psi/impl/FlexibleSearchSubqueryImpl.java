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

public class FlexibleSearchSubqueryImpl extends ASTWrapperPsiElement implements FlexibleSearchSubquery {

  public FlexibleSearchSubqueryImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitSubquery(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FlexibleSearchCorrelationName getCorrelationName() {
    return findChildByClass(FlexibleSearchCorrelationName.class);
  }

  @Override
  @Nullable
  public FlexibleSearchQuerySpecification getQuerySpecification() {
    return findChildByClass(FlexibleSearchQuerySpecification.class);
  }

  @Override
  @NotNull
  public List<FlexibleSearchSubquery> getSubqueryList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FlexibleSearchSubquery.class);
  }

  @Override
  @NotNull
  public PsiElement getLeftDoubleBrace() {
    return findNotNullChildByType(LEFT_DOUBLE_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getRightDoubleBrace() {
    return findChildByType(RIGHT_DOUBLE_BRACE);
  }

}
