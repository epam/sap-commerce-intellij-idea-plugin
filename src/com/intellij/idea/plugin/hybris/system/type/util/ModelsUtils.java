/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModelsUtils {

    private static final Key<Boolean> ITEM_MODEL_KEY = Key.create("Y_IS_ITEM_MODEL_CLASS");
    private static final Key<Boolean> ENUM_MODEL_KEY = Key.create("Y_IS_ENUM_MODEL_CLASS");

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
        final var itemModelFile = psiClass.getUserData(ITEM_MODEL_KEY);
        if (itemModelFile != null) return itemModelFile;

        final var psiFile = psiClass.getContainingFile();

        final VirtualFile virtualFile = psiFile.getVirtualFile() != null
            ? psiFile.getVirtualFile()
            : psiFile.getOriginalFile().getVirtualFile();

        if (virtualFile == null) return storeAndReturn(psiClass, ITEM_MODEL_KEY, false);

        final var extension = virtualFile.getExtension();
        if (extension == null) return storeAndReturn(psiClass, ITEM_MODEL_KEY, false);

        if (!shouldProcessItemType(psiClass, virtualFile, extension)) return storeAndReturn(psiClass, ITEM_MODEL_KEY, false);

        return storeAndReturn(psiClass, ITEM_MODEL_KEY, true);
    }

    public static boolean isEnumFile(final PsiClass psiClass) {
        final var enumModelFile = psiClass.getUserData(ENUM_MODEL_KEY);
        if (enumModelFile != null) return enumModelFile;

        final var psiFile = psiClass.getContainingFile();
        final var virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) return storeAndReturn(psiClass, ENUM_MODEL_KEY, false);

        final var extension = virtualFile.getExtension();
        if (extension == null) return storeAndReturn(psiClass, ENUM_MODEL_KEY, false);

        if (!shouldProcessEnum(psiClass, virtualFile, extension)) return storeAndReturn(psiClass, ENUM_MODEL_KEY, false);

        return storeAndReturn(psiClass, ENUM_MODEL_KEY, true);
    }

    private static boolean storeAndReturn(final PsiClass psiClass, final Key<Boolean> key, final boolean verificationResult) {
        psiClass.putUserData(key, verificationResult);
        return verificationResult;
    }

    private static boolean shouldProcessItemType(
        final PsiClass psiClass,
        final @NotNull VirtualFile virtualFile,
        final @NotNull String extension
    ) {
        if (extension.equals("java") && !virtualFile.getPath().contains(HybrisConstants.BOOTSTRAP_GEN_SRC_PATH)) return false;
        if (extension.equals("class") && !virtualFile.getPath().contains(HybrisConstants.JAR_MODELS)) return false;

        final var className = psiClass.getName();
        if (className == null) return false;
        if (className.endsWith(HybrisConstants.MODEL_SUFFIX)) return true;

        final var superClass = psiClass.getSuperClass();
        return superClass != null
            && superClass.getName() != null
            && superClass.getName().startsWith("Generated");
    }

    private static boolean shouldProcessEnum(
        final PsiClass psiClass,
        final @NotNull VirtualFile virtualFile,
        final @NotNull String extension
    ) {
        if (extension.equals("java") && !virtualFile.getPath().contains(HybrisConstants.BOOTSTRAP_GEN_SRC_PATH)) return false;
        if (extension.equals("class") && !virtualFile.getPath().contains(HybrisConstants.JAR_MODELS)) return false;

        for (final var implementsListType : psiClass.getImplementsListTypes()) {
            if (HybrisConstants.CLASS_NAME_ENUM.equals(implementsListType.getClassName())) {
                return true;
            }
        }

        return false;
    }

}
