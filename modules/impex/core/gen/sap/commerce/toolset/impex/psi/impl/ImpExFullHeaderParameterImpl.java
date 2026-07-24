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
import sap.commerce.toolset.impex.psi.*;
import com.intellij.psi.PsiReference;
import kotlin.jvm.functions.Function0;
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier;
import sap.commerce.toolset.impex.psi.impl.ImpExFullHeaderParameterMixin.ParametersContext;

public class ImpExFullHeaderParameterImpl extends ImpExFullHeaderParameterMixin implements ImpExFullHeaderParameter {

  public ImpExFullHeaderParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ImpExVisitor visitor) {
    visitor.visitFullHeaderParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ImpExVisitor) accept((ImpExVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ImpExAnyHeaderParameterName getAnyHeaderParameterName() {
    return findNotNullChildByClass(ImpExAnyHeaderParameterName.class);
  }

  @Override
  @NotNull
  public List<ImpExModifiers> getModifiersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExModifiers.class);
  }

  @Override
  @NotNull
  public List<ImpExParameters> getParametersList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImpExParameters.class);
  }

  @Override
  public @Nullable ImpExHeaderLine getHeaderLine() {
    return ImpExPsiUtil.getHeaderLine(this);
  }

}
