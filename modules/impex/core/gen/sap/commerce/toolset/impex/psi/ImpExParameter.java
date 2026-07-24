/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExParameter extends PsiElement {

  @Nullable
  ImpExDocumentIdUsage getDocumentIdUsage();

  @NotNull
  List<ImpExMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpExModifiers> getModifiersList();

  @NotNull
  List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList();

  @Nullable
  ImpExSubParameters getSubParameters();

  @Nullable String getReferenceItemTypeName();

  @Nullable String getReferenceName();

  @Nullable String getItemTypeName();

  @Nullable String getInlineTypeName();

  @NotNull String getAttributeName();

  boolean isHeaderAbbreviation();

  @Nullable ImpExHeaderParameterTSContext getTypeSystemContext();

}
