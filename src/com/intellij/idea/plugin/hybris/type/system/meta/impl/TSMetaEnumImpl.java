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

package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnumValue;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class TSMetaEnumImpl extends TSMetaEntityImpl<EnumType> implements TSMetaEnum {

    private final NoCaseMultiMap<TSMetaEnumValueImpl> name2ValueObj = new NoCaseMultiMap<>();

    public TSMetaEnumImpl(final String name, final EnumType dom) {
        super(name, dom);
    }

    public static String extractName(@NotNull final EnumType domEnumType) {
        return domEnumType.getCode().getValue();
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaEnumValue> getValuesStream() {
        return name2ValueObj.values().stream();
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaEnumValue> findValueByName(@NotNull final String name) {
        return new ArrayList<>(name2ValueObj.get(name));
    }

    @Override
    public void createValue(final @NotNull EnumValue domEnumValue) {
        final TSMetaEnumValueImpl result = new TSMetaEnumValueImpl(this, domEnumValue);

        if (result.getName() != null) {
            name2ValueObj.putValue(result.getName(), result);
        }
    }

}
