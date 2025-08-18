/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.psi;

import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Deprecated(since = "Convert to kotlin util methods")
public final class PsiUtils {

    private PsiUtils() {
    }

    public static boolean shouldCreateNewReference(final @Nullable PsiReference reference, final String text) {
        if (reference == null) return true;

        if (reference instanceof final PsiReferenceBase psiReferenceBase) {
            return text != null
                && (text.length() != reference.getRangeInElement().getLength() || !text.equals(psiReferenceBase.getValue()));
        } else {
            return false;
        }
    }

    @NotNull
    public static ResolveResult[] getValidResults(final ResolveResult[] resolveResults) {
        return Arrays.stream(resolveResults)
            .filter(ResolveResult::isValidResult)
            .toArray(ResolveResult[]::new);
    }
}
