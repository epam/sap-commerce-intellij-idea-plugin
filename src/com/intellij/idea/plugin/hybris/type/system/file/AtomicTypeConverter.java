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
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAtomic;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
public class AtomicTypeConverter extends TypeSystemConverterBase<AtomicType> {

    public AtomicTypeConverter() {
        super(AtomicType.class);
    }


    @Override
    protected AtomicType searchForName(
        @NotNull final String name, @NotNull final ConvertContext context, final TSMetaModelService meta
    ) {
        return Optional.ofNullable(meta.findMetaAtomicByName(name))
                       .map(TSMetaAtomic::retrieveDom)
                       .orElse(null);
    }

    @Override
    protected Collection<? extends AtomicType> searchAll(
        @NotNull final ConvertContext context, final TSMetaModelService meta
    ) {
        return meta.<TSMetaAtomic>getAll(MetaType.META_ATOMIC).stream()
                   .map(TSMetaAtomic::retrieveDom)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String toString(@Nullable final AtomicType t, final ConvertContext context) {
        return useAttributeValue(t, AtomicType::getClazz);
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(@Nullable final AtomicType resolvedValue) {
        return navigateToValue(resolvedValue, AtomicType::getClazz);
    }
}
