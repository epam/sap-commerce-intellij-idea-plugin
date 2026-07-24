/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import kotlin.jvm.functions.Function0;
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier;
import sap.commerce.toolset.impex.psi.impl.ImpExFullHeaderParameterMixin.ParametersContext;

public interface ImpExFullHeaderParameter extends PsiElement {

  @NotNull
  ImpExAnyHeaderParameterName getAnyHeaderParameterName();

  @NotNull
  List<ImpExModifiers> getModifiersList();

  @NotNull
  List<ImpExParameters> getParametersList();

  @Nullable ImpExHeaderLine getHeaderLine();

  int getColumnNumber();

  @Nullable ImpExAttribute getAttribute(@NotNull AttributeModifier attributeModifier);

  @NotNull String getAttributeValue(@NotNull AttributeModifier attributeModifier, @NotNull String defaultValue);

  @NotNull List<@NotNull ImpExValueGroup> getValueGroups();

  boolean isUnique();

  @Nullable ImpExHeaderParameterTSContext getTypeSystemContext();

  @NotNull List<@NotNull ImpExDocumentIdUsage> getDocIdUsages();

  @NotNull PsiReference @Nullable [] collectDocIdReferences(@NotNull PsiElement targetElement, @NotNull ImpExHeaderParameterTSContext tsContext);

  @NotNull PsiReference @Nullable [] collectTSReferences(@NotNull PsiElement targetElement, @NotNull ImpExHeaderParameterTSContext tsContext, @NotNull Function0<@NotNull PsiElement @NotNull []> valuesProvider);

  @Nullable String resolveDefaultValue();

  @NotNull ParametersContext getParametersContext();

}
