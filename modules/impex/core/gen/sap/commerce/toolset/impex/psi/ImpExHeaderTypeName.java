/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExHeaderTypeName extends PsiElement {

  @NotNull
  List<ImpExMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList();

  @Nullable ImpExHeaderLine getHeaderLine();

}
