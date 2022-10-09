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

import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public interface TSMetaClass extends TSMetaClassifier<ItemType> {

    String IMPLICIT_SUPER_CLASS_NAME = "GenericItem";

    @Nullable
    String getExtendedMetaClassName();

    @NotNull
    TSMetaModel getMetaModel();

    void addProperty(final String key, TSMetaProperty property);

    @NotNull
    Stream<? extends TSMetaProperty> getPropertiesStream(boolean includeInherited);

    @NotNull
    Collection<? extends TSMetaProperty> findPropertiesByName(@NotNull String name, boolean includeInherited);

    @NotNull
    Collection<? extends TSMetaReference.ReferenceEnd> findReferenceEndsByRole(
        @NotNull String role,
        boolean includeInherited
    );

    @NotNull
    Stream<? extends TSMetaReference.ReferenceEnd> getReferenceEndsStream(boolean includeInherited);

    String getTypeCode();

    @NotNull
    Stream<? extends ItemType> retrieveAllDomsStream();

}
