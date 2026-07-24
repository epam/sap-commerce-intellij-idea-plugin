/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExAnyHeaderParameterName extends PsiElement {

  @Nullable
  ImpExDocumentIdDec getDocumentIdDec();

  @NotNull
  List<ImpExMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList();

  @Nullable
  ImpExSpecialParameter getSpecialParameter();

  @Nullable ImpExHeaderTypeName getHeaderItemTypeName();

  boolean isHeaderAbbreviation();

}
