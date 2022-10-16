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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCollection;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TSMetaCollectionImpl extends TSMetaEntityImpl<CollectionType> implements TSMetaCollection {

    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final String myElementType;
    private final Type myType;

    public TSMetaCollectionImpl(final Project project, final String name, final CollectionType dom) {
        super(project, name, dom);
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myElementType = dom.getElementType().getStringValue();
        myType = Optional.ofNullable(dom.getType().getValue())
            .orElse(Type.COLLECTION);
    }

    @Nullable
    @Override
    public Type getType() {
        return myType;
    }

    @Nullable
    @Override
    public String getElementType() {
        return myElementType;
    }

    @Override
    public boolean isAutoCreate() {
        return myAutoCreate;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }
}
