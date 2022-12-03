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
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.intellij.openapi.util.io.FileUtil.normalize;

/**
 * Created 6:46 PM 18 September 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public final class TypeSystemUtils {

    public static boolean isTypeSystemXmlFile(@Nullable final PsiFile psiFile) {
        return psiFile instanceof XmlFile && psiFile.getName().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING);
    }

    public static boolean isAttributeGenerationDisabled(@NotNull final Attribute attribute) {
        Boolean isGenerate = TypeSystemUtils.getBoolean(attribute.getGenerate());
        if (Boolean.FALSE.equals(isGenerate)) {
            return true;
        }

        isGenerate = TypeSystemUtils.getBoolean(attribute.getModel().getGenerate());

        return Boolean.FALSE.equals(isGenerate);
    }

    public static boolean isClassGenerationDisabled(@NotNull final ItemType itemType) {
        Boolean isGenerate = TypeSystemUtils.getBoolean(itemType.getGenerate());
        if (Boolean.FALSE.equals(isGenerate)) {
            return true;
        }

        isGenerate = TypeSystemUtils.getBoolean(itemType.getModel().getGenerate());
        return Boolean.FALSE.equals(isGenerate);
    }

    public static boolean isEnumGenerationDisabled(@NotNull final EnumType enumType) {
        final Boolean isGenerate = TypeSystemUtils.getBoolean(enumType.getGenerate());

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

    @Nullable
    public static Boolean getBoolean(@Nullable GenericAttributeValue genericAttributeValue) {
        if (null == genericAttributeValue) {
            return null;
        } else {
            final XmlAttributeValue xmlAttributeValue = genericAttributeValue.getXmlAttributeValue();
            if (null == xmlAttributeValue) {
                return null;
            } else {
                final String value = xmlAttributeValue.getValue();
                return StringUtils.isBlank(value) ? null : Boolean.valueOf(value);
            }
        }
    }

    public static boolean isTsFile(@NotNull final PsiFile file) {
        return file instanceof XmlFile && DomManager.getDomManager(file.getProject()).getFileElement(
            (XmlFile) file,
            Items.class
        ) != null;
    }

    public static boolean isCustomExtensionFile(@NotNull final PsiFile file) {
        if (!isTsFile(file)) {
            return false;
        }

        final VirtualFile vFile = file.getVirtualFile();
        return vFile != null && PsiUtils.isCustomExtensionFile(vFile, file.getProject());
    }

    @Nullable
    public static Module getModuleForFile(@NotNull final PsiFile file) {
        if (!isTsFile(file)) {
            return null;
        }

        return PsiUtils.getModule(file);
    }

    /*
     * This method disqualifies known hybris extensions
     */
    private static boolean estimateIsCustomExtension(@NotNull final VirtualFile file) {
        final File itemsFile = VfsUtilCore.virtualToIoFile(file);
        final String itemsFilePath = normalize(itemsFile.getAbsolutePath());

        if (itemsFilePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX))) {
            return false;
        }
        if (itemsFilePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019))) {
            return false;
        }
        if (itemsFilePath.contains(normalize(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX))) {
            return false;
        }
        return true;
    }

}
