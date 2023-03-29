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
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils;
import com.intellij.idea.plugin.hybris.system.type.model.Attribute;
import com.intellij.idea.plugin.hybris.system.type.model.EnumType;
import com.intellij.idea.plugin.hybris.system.type.model.ItemType;
import com.intellij.idea.plugin.hybris.system.type.model.Items;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 6:46 PM 18 September 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public final class TSUtils {

    private TSUtils() {
    }

    public static boolean isAttributeGenerationDisabled(@NotNull final Attribute attribute) {
        Boolean isGenerate = attribute.getGenerate().getValue();
        if (Boolean.FALSE.equals(isGenerate)) {
            return true;
        }

        isGenerate = attribute.getModel().getGenerate().getValue();

        return Boolean.FALSE.equals(isGenerate);
    }

    public static boolean isClassGenerationDisabled(@NotNull final ItemType itemType) {
        Boolean isGenerate = itemType.getGenerate().getValue();
        if (Boolean.FALSE.equals(isGenerate)) {
            return true;
        }

        isGenerate = itemType.getModel().getGenerate().getValue();
        return Boolean.FALSE.equals(isGenerate);
    }

    public static boolean isEnumGenerationDisabled(@NotNull final EnumType enumType) {
        final Boolean isGenerate = enumType.getGenerate().getValue();

        return Boolean.FALSE.equals(isGenerate);
    }

    @Nullable
    public static String getString(@Nullable GenericAttributeValue genericAttributeValue) {
        if (null == genericAttributeValue) {
            return null;
        } else {
            final XmlAttributeValue xmlAttributeValue = genericAttributeValue.getXmlAttributeValue();
            if (null == xmlAttributeValue) {
                return null;
            } else {
                final String value = xmlAttributeValue.getValue();
                return StringUtils.isBlank(value) ? null : value;
            }
        }
    }

    public static boolean isTypeSystemFile(@NotNull final PsiFile file) {
        return file instanceof XmlFile
            && file.getName().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING)
            && DomManager.getDomManager(file.getProject()).getFileElement((XmlFile) file, Items.class) != null;
    }

    public static boolean isCustomExtensionFile(@NotNull final PsiFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            if (!isTypeSystemFile(file)) {
                return CachedValueProvider.Result.create(false, file);
            }


            final VirtualFile vFile = file.getVirtualFile();
            return CachedValueProvider.Result.create(vFile != null && PsiUtils.isCustomExtensionFile(vFile, file.getProject()), file);
        });
    }

    @Nullable
    public static Module getModuleForFile(@NotNull final PsiFile file) {
        if (!isTypeSystemFile(file)) {
            return null;
        }

        return PsiUtils.getModule(file);
    }

}
