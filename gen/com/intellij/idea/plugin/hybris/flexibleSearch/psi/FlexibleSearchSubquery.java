// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FlexibleSearchSubquery extends PsiElement {

  @Nullable
  FlexibleSearchCorrelationName getCorrelationName();

  @Nullable
  FlexibleSearchQuerySpecification getQuerySpecification();

  @NotNull
  List<FlexibleSearchSubquery> getSubqueryList();

  @NotNull
  PsiElement getLeftDoubleBrace();

  @Nullable
  PsiElement getRightDoubleBrace();

}
