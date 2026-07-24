/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExValueLine extends PsiElement {

  @Nullable
  ImpExSubTypeName getSubTypeName();

  @NotNull
  List<ImpExValueGroup> getValueGroupList();

  @Nullable ImpExHeaderLine getHeaderLine();

  @Nullable ImpExValueGroup getValueGroup(int columnNumber);

  void addValueGroups(int groupsToAdd);

}
