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

public interface ImpExUserRightsHeaderParameter extends PsiElement {

  @Nullable ImpExUserRightsHeaderLine getHeaderLine();

  @Nullable Integer getColumnNumber();

  @NotNull Collection<@NotNull ImpExUserRightsValueGroup> getValueGroups();

}
