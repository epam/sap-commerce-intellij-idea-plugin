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

package com.intellij.idea.plugin.hybris.system.type.file;

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaCollection;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaCollection;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType;
import com.intellij.idea.plugin.hybris.system.type.model.CollectionType;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectionTypeConverter extends TSConverterBase<CollectionType> {

    public CollectionTypeConverter() {
        super(CollectionType.class);
    }

    @Override
    protected CollectionType searchForName(
        @NotNull final String name, @NotNull final ConvertContext context, final TSMetaModelAccess meta
    ) {
        return Optional.ofNullable(meta.findMetaCollectionByName(name))
                       .map(TSMetaCollection::retrieveDom)
                       .orElse(null);
    }

    @Override
    protected Collection<? extends CollectionType> searchAll(
        @NotNull final ConvertContext context, final TSMetaModelAccess meta
    ) {
        return meta.<TSGlobalMetaCollection>getAll(TSMetaType.META_COLLECTION).stream()
                   .map(TSMetaCollection::retrieveDom)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String toString(@Nullable final CollectionType t, final ConvertContext context) {
        return useAttributeValue(t, CollectionType::getCode);
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(@Nullable final CollectionType resolvedValue) {
        return navigateToValue(resolvedValue, CollectionType::getCode);
    }
}
