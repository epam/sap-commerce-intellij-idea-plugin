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

package com.intellij.idea.plugin.hybris.type.system.meta;

import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaClassImpl;
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public interface TSMetaModel {

    void collectReferencesForSourceType(
        @NotNull TSMetaClassImpl source,
        @NotNull Collection<TSMetaReference.ReferenceEnd> out
    );

    @NotNull
    Stream<? extends TSMetaClass> getMetaClassesStream();

    @NotNull
    Stream<? extends TSMetaAtomic> getMetaAtomicStream();

    @NotNull
    Stream<? extends TSMetaEnum> getMetaEnumsStream();

    @NotNull
    Stream<? extends TSMetaCollection> getMetaCollectionsStream();

    @NotNull
    Stream<? extends TSMetaReference> getMetaRelationsStream();

    @Nullable
    default TSMetaClassifier<? extends DomElement> findMetaClassifierByName(final @NotNull String name) {
        TSMetaClassifier<? extends DomElement> result = findMetaClassByName(name);
        if (result == null) {
            result = findMetaCollectionByName(name);
        }
        if (result == null) {
            result = findMetaEnumByName(name);
        }
        return result;
    }

    @Nullable
    TSMetaClass findMetaClassByName(@NotNull String name);

    @Nullable
    TSMetaEnum findMetaEnumByName(@NotNull String name);

    @Nullable
    TSMetaAtomic findMetaAtomicByName(@NotNull String name);

    @Nullable
    TSMetaCollection findMetaCollectionByName(@NotNull String name);

    @Nullable
    TSMetaClass findMetaClassForDom(@NotNull ItemType dom);

    @Nullable
    TSMetaAtomic findOrCreateAtomicType(@NotNull AtomicType atomicType);

    List<TSMetaReference> findRelationByName(@NotNull final String name);
}
