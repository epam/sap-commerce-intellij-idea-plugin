/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 */
package sap.commerce.toolset.impex.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.TextRange;
import java.util.Collection;
import sap.commerce.toolset.impex.codeInspection.context.ImpExColumnContext;
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationContext;
import sap.commerce.toolset.psi.RangeAwareContent;

public interface ImpExHeaderLine extends PsiElement {

  @NotNull
  ImpExAnyHeaderMode getAnyHeaderMode();

  @NotNull
  List<ImpExFullHeaderParameter> getFullHeaderParameterList();

  @Nullable
  ImpExFullHeaderType getFullHeaderType();

  @Nullable ImpExFullHeaderParameter getFullHeaderParameter(@NotNull String parameterName);

  @Nullable ImpExFullHeaderParameter getFullHeaderParameter(int index);

  @NotNull Collection<@NotNull ImpExValueLine> getValueLines();

  @NotNull TextRange getTableRange();

  @NotNull List<@NotNull ImpExFullHeaderParameter> getUniqueFullHeaderParameters();

  boolean hasDocumentIdDec();

  @NotNull List<@NotNull ImpExDocumentIdDec> getDocumentIdDeclarations();

  @Nullable RangeAwareContent generateDocId(@NotNull ImpExDocIdGenerationContext context);

  @NotNull List<@NotNull ImpExColumnContext> getColumnContexts();

}
