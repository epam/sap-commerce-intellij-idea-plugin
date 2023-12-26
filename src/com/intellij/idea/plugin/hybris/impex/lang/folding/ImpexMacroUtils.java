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

package com.intellij.idea.plugin.hybris.impex.lang.folding;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;

import java.util.HashMap;
import java.util.Map;

public final class ImpexMacroUtils {

    private static final Key<CachedValue<Map<String, ImpexMacroDescriptor>>> FILE_IMPEX_FOLDING_CACHE_KEY = Key.create("FILE_IMPEX_FOLDING_CACHE");

    private ImpexMacroUtils() {
    }

    public static CachedValue<Map<String, ImpexMacroDescriptor>> getFileCache(final PsiFile impexFile) {
        CachedValue<Map<String, ImpexMacroDescriptor>> fileModelCache = impexFile.getUserData(FILE_IMPEX_FOLDING_CACHE_KEY);

        if (fileModelCache == null) {
            fileModelCache = CachedValuesManager.getManager(impexFile.getProject()).createCachedValue(
                () -> ApplicationManager.getApplication().runReadAction(
                    (Computable<CachedValueProvider.Result<Map<String, ImpexMacroDescriptor>>>) () ->
                        CachedValueProvider.Result.create(new HashMap<>(), impexFile)
                ), false);
            impexFile.putUserData(FILE_IMPEX_FOLDING_CACHE_KEY, fileModelCache);
        }

        return fileModelCache;
    }

}
