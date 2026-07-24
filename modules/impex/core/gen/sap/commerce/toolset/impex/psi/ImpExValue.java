/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExValue extends ImpExPsiNamedElement {

  @NotNull
  List<ImpExMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList();

  @Nullable
  ImpExString getString();

  @Nullable ImpExValueGroup getValueGroup();

  @Nullable PsiElement getFieldValue(int index);

  boolean isImportable();

  boolean isNonImportable();

  boolean isQuotable();

  boolean isNotQuotable();

}
