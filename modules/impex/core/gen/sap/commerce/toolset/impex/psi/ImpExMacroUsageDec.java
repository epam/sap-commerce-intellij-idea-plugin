/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.util.Set;
import sap.commerce.toolset.impex.psi.impl.ImpExMacroUsageDecMixin;

public interface ImpExMacroUsageDec extends ImpExPsiNamedElement {

  @NotNull ImpExMacroUsageDecMixin getNameIdentifier();

  @Nullable String getConfigPropertyKey();

  @NotNull String resolveValue(@NotNull Set<@Nullable ImpExMacroUsageDec> evaluatedMacroUsages);

}
