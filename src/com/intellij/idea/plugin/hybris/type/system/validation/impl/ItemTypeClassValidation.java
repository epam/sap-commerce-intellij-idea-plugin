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

package com.intellij.idea.plugin.hybris.type.system.validation.impl;

import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.Attributes;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.utils.TSUtils;
import com.intellij.idea.plugin.hybris.type.system.validation.AbstractTSClassesValidation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.MODEL_SUFFIX;
import static com.intellij.idea.plugin.hybris.type.system.utils.TSUtils.getString;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class ItemTypeClassValidation extends AbstractTSClassesValidation<ItemType, Attribute> {

    @NotNull
    @Override
    public String buildGeneratedClassName(@NotNull final ItemType itemType) {
        final String value = getString(itemType.getCode());

        return null == value ? StringUtils.EMPTY : value + MODEL_SUFFIX;
    }

    @NotNull
    @Override
    public String buildJavaFieldName(@NotNull final Attribute itemAttribute) {
        final String value = getString(itemAttribute.getQualifier());
        return null == value ? StringUtils.EMPTY : value;
    }

    @NotNull
    @Override
    public List<Attribute> getDefinedAttributes(@NotNull final ItemType itemType) {
        final Attributes attributes = itemType.getAttributes();

        return attributes.getAttributes();
    }

    @Override
    public boolean isJavaClassGenerationDisabledForItemType(@NotNull final ItemType itemType) {
        return TSUtils.isClassGenerationDisabled(itemType);
    }

    @Override
    protected boolean isJavaFieldGenerationDisabled(@NotNull final Attribute itemAttribute) {
        return TSUtils.isAttributeGenerationDisabled(itemAttribute);
    }
}
