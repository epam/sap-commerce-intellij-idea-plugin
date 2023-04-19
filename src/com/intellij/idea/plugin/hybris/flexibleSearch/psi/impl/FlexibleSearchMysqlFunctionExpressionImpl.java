// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*;

public class FlexibleSearchMysqlFunctionExpressionImpl extends FlexibleSearchExpressionImpl implements FlexibleSearchMysqlFunctionExpression {

  public FlexibleSearchMysqlFunctionExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitMysqlFunctionExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FlexibleSearchExpression getExpression() {
    return findChildByClass(FlexibleSearchExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
