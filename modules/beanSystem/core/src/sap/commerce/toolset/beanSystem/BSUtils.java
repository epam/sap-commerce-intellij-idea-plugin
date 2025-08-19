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

package sap.commerce.toolset.beanSystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import kotlin.Deprecated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.beanSystem.model.Beans;
import sap.commerce.toolset.project.ModuleUtil;

@Deprecated(message = "Convert to kotlin")
public final class BSUtils {

    private BSUtils() {
    }

    public static @Nullable Module getModuleForFile(@NotNull final PsiFile file) {
        if (!isBeansXmlFile(file)) {
            return null;
        }

        return ModuleUtil.getModule(file);
    }


    public static boolean isBeansXmlFile(@NotNull final PsiFile file) {
        return file instanceof XmlFile
            && isBeansXmlFile((XmlFile) file);
    }

    public static boolean isBeansXmlFile(@NotNull final XmlFile file) {
        return file.getName().endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING)
            && DomManager.getDomManager(file.getProject()).getFileElement(file, Beans.class) != null;
    }

    public static boolean isCustomExtensionFile(@NotNull final PsiFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            if (!isBeansXmlFile(file)) {
                return CachedValueProvider.Result.create(false, file);
            }


            final VirtualFile vFile = file.getVirtualFile();
            return CachedValueProvider.Result.create(vFile != null && ModuleUtil.isCustomExtensionFile(vFile, file.getProject()), file);
        });
    }

}
