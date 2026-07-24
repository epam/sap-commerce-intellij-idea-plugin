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
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import sap.commerce.toolset.impex.psi.*;

public class ImpExMacroDeclarationImpl extends ASTWrapperPsiElement implements ImpExMacroDeclaration {

  public ImpExMacroDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitMacroDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpExMacroNameDec getMacroNameDec() {
    return findNotNullChildByClass(ImpExMacroNameDec.class);
  }

  @Override
  @Nullable
  public ImpExMacroValuesDec getMacroValuesDec() {
    return findChildByClass(ImpExMacroValuesDec.class);
  }

}
