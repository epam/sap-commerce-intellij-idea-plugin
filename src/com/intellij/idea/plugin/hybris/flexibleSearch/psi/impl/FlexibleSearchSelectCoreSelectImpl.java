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

public class FlexibleSearchSelectCoreSelectImpl extends ASTWrapperPsiElement implements FlexibleSearchSelectCoreSelect {

  public FlexibleSearchSelectCoreSelectImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull FlexibleSearchVisitor visitor) {
    visitor.visitSelectCoreSelect(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof FlexibleSearchVisitor) accept((FlexibleSearchVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FlexibleSearchFromClause getFromClause() {
    return findChildByClass(FlexibleSearchFromClause.class);
  }

  @Override
  @Nullable
  public FlexibleSearchGroupByClause getGroupByClause() {
    return findChildByClass(FlexibleSearchGroupByClause.class);
  }

  @Override
  @NotNull
  public FlexibleSearchResultColumns getResultColumns() {
    return findNotNullChildByClass(FlexibleSearchResultColumns.class);
  }

  @Override
  @Nullable
  public FlexibleSearchWhereClause getWhereClause() {
    return findChildByClass(FlexibleSearchWhereClause.class);
  }

}
