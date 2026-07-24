/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExGroovyScriptBodyMultiline extends ImpExScriptBody {

  @NotNull
  List<ImpExMacroUsageDec> getMacroUsageDecList();

  @NotNull
  List<ImpExPossibleMacroUsageDec> getPossibleMacroUsageDecList();

  @NotNull
  List<ImpExString> getStringList();

}
