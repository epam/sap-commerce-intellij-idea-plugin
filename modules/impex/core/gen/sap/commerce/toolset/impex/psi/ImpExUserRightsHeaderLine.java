/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExUserRightsHeaderLine extends PsiElement {

  @NotNull
  List<ImpExUserRightsHeaderParameter> getUserRightsHeaderParameterList();

  @Nullable ImpExUserRightsHeaderParameter getHeaderParameter(int index);

}
