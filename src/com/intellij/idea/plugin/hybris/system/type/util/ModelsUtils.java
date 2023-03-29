/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.type.util;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import org.jetbrains.annotations.Nullable;

public final class ModelsUtils {

    private ModelsUtils() {
    }

    public static @Nullable String cleanSearchName(@Nullable final String searchName) {
        if (searchName == null) return null;

        final var idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX);

        return idx == -1
            ? searchName
            : searchName.substring(0, idx);
    }

    public static boolean isItemModelFile(final PsiClass psiClass) {
        final var psiFile = psiClass.getContainingFile();

        if (psiFile.getText().contains("Generated model class for type")) {
            return true;
        }

        final VirtualFile virtualFile = psiFile.getVirtualFile() != null
            ? psiFile.getVirtualFile()
            : psiFile.getOriginalFile().getVirtualFile();

        if (virtualFile == null || virtualFile.getExtension() == null) {
            return false;
        }

        return virtualFile.getExtension().equals("class")
               && virtualFile.getPath().contains("models.jar")
               && shouldProcessItemType(psiClass);
    }

    private static boolean shouldProcessItemType(final PsiClass psiClass) {
        return psiClass.getName() != null && psiClass.getName().endsWith(HybrisConstants.MODEL_SUFFIX)
               || (psiClass.getSuperClass() != null
                   && psiClass.getSuperClass().getName() != null
                   && psiClass.getSuperClass().getName().startsWith("Generated"));
    }

    public static boolean isEnumFile(final PsiClass psiClass) {
        final var psiFile = psiClass.getContainingFile();

        if (psiFile.getText().contains("Generated enum")) {
            return true;
        }

        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile.getExtension() == null) {
            return false;
        }

        return virtualFile.getExtension().equals("class")
               && virtualFile.getPath().contains("models.jar")
               && shouldProcessEnum(psiClass);
    }

    private static boolean shouldProcessEnum(final PsiClass psiClass) {
        final PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();

        for (final PsiClassType implementsListType : implementsListTypes) {
            if ("HybrisEnumValue".equals(implementsListType.getClassName())) {
                return true;
            }
        }

        return false;
    }

}
