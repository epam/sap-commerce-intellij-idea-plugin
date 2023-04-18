// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FlexibleSearchFromClause extends PsiElement {

  @NotNull
  List<FlexibleSearchJoinConstraint> getJoinConstraintList();

  @NotNull
  List<FlexibleSearchJoinOperator> getJoinOperatorList();

  @NotNull
  List<FlexibleSearchTableOrSubquery> getTableOrSubqueryList();

}
