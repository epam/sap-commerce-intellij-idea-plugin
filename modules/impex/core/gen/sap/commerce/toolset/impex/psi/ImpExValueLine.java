/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.DomElement;
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier;

public interface ImpExValueLine extends PsiElement {

  @Nullable
  ImpExSubTypeName getSubTypeName();

  @NotNull
  List<ImpExValueGroup> getValueGroupList();

  @Nullable ImpExHeaderLine getHeaderLine();

  @Nullable ImpExValueGroup getValueGroup(int columnNumber);

  @Nullable TSGlobalMetaClassifier<? extends @NotNull DomElement> getMetaType();

  void addValueGroups(int groupsToAdd);

}
