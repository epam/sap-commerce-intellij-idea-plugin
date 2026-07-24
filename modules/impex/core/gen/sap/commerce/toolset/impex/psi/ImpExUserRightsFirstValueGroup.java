/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ImpExUserRightsFirstValueGroup extends PsiElement {

  @Nullable
  ImpExUserRightsAttributeValue getUserRightsAttributeValue();

  @Nullable
  ImpExUserRightsMultiValue getUserRightsMultiValue();

  @Nullable
  ImpExUserRightsPermissionValue getUserRightsPermissionValue();

  @Nullable
  ImpExUserRightsSingleValue getUserRightsSingleValue();

  @Nullable ImpExValueLine getValueLine();

}
