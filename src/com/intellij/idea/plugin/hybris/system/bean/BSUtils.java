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

package com.intellij.idea.plugin.hybris.system.bean;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BSUtils {

    private BSUtils() {
    }

    public static @Nullable Module getModuleForFile(@NotNull final PsiFile file) {
        if (!isBeansXmlFile(file)) {
            return null;
        }

        return PsiUtils.getModule(file);
    }


    public static boolean isBeansXmlFile(@NotNull final PsiFile file) {
        return isBeansXmlFile(file.getName());
    }

    private static boolean isBeansXmlFile(@NotNull final String name) {
        return name.endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING);
    }

    public static boolean isBeansXmlFile(@NotNull final XmlFile file) {
        return isBeansXmlFile(file.getName());
    }

    public static boolean isCustomExtensionFile(@NotNull final PsiFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            if (!isBeansXmlFile(file)) {
                return CachedValueProvider.Result.create(false, file);
            }


            final VirtualFile vFile = file.getVirtualFile();
            return CachedValueProvider.Result.create(vFile != null && PsiUtils.isCustomExtensionFile(vFile, file.getProject()), file);
        });
    }

}
