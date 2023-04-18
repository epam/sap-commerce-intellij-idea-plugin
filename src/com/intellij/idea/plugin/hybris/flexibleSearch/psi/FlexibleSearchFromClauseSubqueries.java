// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FlexibleSearchFromClauseSubqueries extends PsiElement {

  @NotNull
  List<FlexibleSearchCompoundOperator> getCompoundOperatorList();

  @NotNull
  List<FlexibleSearchSelectSubqueryCombined> getSelectSubqueryCombinedList();

  @Nullable
  FlexibleSearchTableAliasName getTableAliasName();

}
