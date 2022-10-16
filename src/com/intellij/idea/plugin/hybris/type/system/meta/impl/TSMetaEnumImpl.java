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
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class TSMetaEnumImpl extends TSMetaEntityImpl<EnumType> implements TSMetaEnum {

    private final NoCaseMultiMap<TSMetaEnumValueImpl> name2ValueObj = new NoCaseMultiMap<>();
    private final boolean myAutocreate;
    private final boolean myGenerate;
    private final boolean myDynamic;
    private final String myDescription;
    private final String myJaloclass;

    public TSMetaEnumImpl(final Project project, final String name, final EnumType dom) {
        super(project, name, dom);
        myAutocreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myDynamic = Boolean.TRUE.equals(dom.getDynamic().getValue());
        myDescription = dom.getDescription().getStringValue();
        myJaloclass = dom.getJaloClass().getStringValue();
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
        final TSMetaEnumValueImpl result = new TSMetaEnumValueImpl(getProject(), this, domEnumValue);

        if (result.getName() != null) {
            name2ValueObj.putValue(result.getName(), result);
        }
    }

    @Override
    public boolean isAutocreate() {
        return myAutocreate;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }

    @Override
    public boolean isDynamic() {
        return myDynamic;
    }

    @Override
    public String getDescription() {
        return myDescription;
    }

    @Override
    public String getJaloclass() {
        return myJaloclass;
    }
}
