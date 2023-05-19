// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.idea.plugin.hybris.impex.psi.*;

public class ImpexMacroValueImpl extends ImpexMacrosValueMixin implements ImpexMacroValue {

  public ImpexMacroValueImpl(ASTNode node) {
    super(node);
  }


  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpexVisitor) accept((ImpexVisitor)visitor);
    else super.accept(visitor);
  }

}
