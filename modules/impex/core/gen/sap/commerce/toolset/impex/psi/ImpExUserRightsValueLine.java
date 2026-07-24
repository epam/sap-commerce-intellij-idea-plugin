/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExUserRightsValueLine extends PsiElement {

  @Nullable
  ImpExUserRightsFirstValueGroup getUserRightsFirstValueGroup();

  @NotNull
  List<ImpExUserRightsValueGroup> getUserRightsValueGroupList();

  @Nullable ImpExUserRightsValueGroup getValueGroup(int index);

  @Nullable ImpExUserRightsHeaderLine getHeaderLine();

}
