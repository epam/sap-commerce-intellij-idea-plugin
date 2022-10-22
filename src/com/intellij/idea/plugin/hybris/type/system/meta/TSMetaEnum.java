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

import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface TSMetaEnum extends TSMetaClassifier<EnumType>, TSMetaSelfMerge<TSMetaEnum> {

    @NotNull
    Collection<? extends TSMetaEnumValue> getValues();

    @NotNull
    Collection<? extends TSMetaEnumValue> findValueByName(@NotNull String name);

    void createValue(@NotNull EnumValue domEnumValue);

    boolean isAutoCreate();

    boolean isGenerate();

    boolean isDynamic();

    boolean isCustom();

    String getDescription();

    String getJaloClass();

    interface TSMetaEnumValue {
        Module getModule();

        @Nullable
        String getName();

        @Nullable
        EnumValue retrieveDom();

        @NotNull
        TSMetaEnum getOwner();

        boolean isCustom();
    }
}
