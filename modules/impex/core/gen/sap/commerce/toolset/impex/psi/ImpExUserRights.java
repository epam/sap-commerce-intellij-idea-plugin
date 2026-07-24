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

public interface ImpExUserRights extends PsiElement {

  @Nullable
  ImpExUserRightsEnd getUserRightsEnd();

  @Nullable
  ImpExUserRightsHeaderLine getUserRightsHeaderLine();

  @NotNull
  ImpExUserRightsStart getUserRightsStart();

  @NotNull
  List<ImpExUserRightsValueLine> getUserRightsValueLineList();

  @NotNull Collection<@NotNull ImpExUserRightsValueGroup> getValueGroups(int index);

}
