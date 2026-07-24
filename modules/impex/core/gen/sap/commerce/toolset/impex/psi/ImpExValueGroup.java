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

public interface ImpExValueGroup extends PsiElement {

  @Nullable
  ImpExValue getValue();

  @Nullable ImpExFullHeaderParameter getFullHeaderParameter();

  int getColumnNumber();

  @Nullable ImpExValueLine getValueLine();

  @Nullable String rawValue();

  @Nullable String resolveValue();

  @Nullable TSGlobalMetaClassifier<? extends @NotNull DomElement> getValueLineMetaType();

}
