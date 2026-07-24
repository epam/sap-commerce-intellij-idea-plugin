/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.util.Collection;
import java.util.Map;

public interface ImpExDocumentIdDec extends ImpExPsiNamedElement {

  @NotNull Map<@NotNull String, @NotNull Collection<@NotNull ImpExValue>> getValues();

  @Nullable ImpExHeaderTypeName getHeaderType();

}
