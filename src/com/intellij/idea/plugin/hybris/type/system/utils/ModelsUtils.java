/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.utils;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public final class ModelsUtils {

    private ModelsUtils() {
    }

    public static boolean isModelFile(final PsiFile psiFile, final @Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }

        if (psiFile.getText().contains("Generated model class for type")) {
            return true;
        }

        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile.getExtension() == null) {
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

    public static boolean isEnumFile(final PsiFile psiFile, final @Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }

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
