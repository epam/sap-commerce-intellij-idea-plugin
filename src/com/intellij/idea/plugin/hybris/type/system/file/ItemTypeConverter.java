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

package com.intellij.idea.plugin.hybris.type.system.file;

import com.intellij.idea.plugin.hybris.type.system.meta.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class ItemTypeConverter extends TypeSystemConverterBase<ItemType> {

    public ItemTypeConverter() {
        super(ItemType.class);
    }

    @Override
    protected ItemType searchForName(
        @NotNull final String name, @NotNull final ConvertContext context, final TSMetaModelService meta
    ) {
        return Optional.ofNullable(meta.findMetaClassByName(name))
                       .map(TSMetaClass::retrieveAllDomsStream)
                       .orElse(Stream.empty())
                       .findFirst()
                       .orElse(null);
    }

    @Override
    protected Collection<? extends ItemType> searchAll(
        @NotNull final ConvertContext context, final TSMetaModelService meta
    ) {
        return meta.<TSMetaClass>getAll(MetaType.META_CLASS).stream()
                   .map(TSMetaClass::retrieveAllDomsStream)
                   .map(Stream::findFirst)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String toString(@Nullable final ItemType t, final ConvertContext context) {
        return useAttributeValue(t, ItemType::getCode);
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(@Nullable final ItemType resolvedValue) {
        return navigateToValue(resolvedValue, ItemType::getCode);
    }
}
