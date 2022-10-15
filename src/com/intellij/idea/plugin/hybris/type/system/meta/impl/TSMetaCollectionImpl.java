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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCollection;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TSMetaCollectionImpl extends TSMetaEntityImpl<CollectionType> implements TSMetaCollection {

    public TSMetaCollectionImpl(final Project project, final String name, final CollectionType dom) {
        super(project, name, dom);
    }

    @Nullable
    @Override
    public Type getType() {
        return Optional.ofNullable(retrieveDom())
                       .map(dom -> dom.getType().getValue())
                       .orElse(null);
    }

    @Nullable
    @Override
    public String getElementTypeName() {
        return Optional.ofNullable(retrieveDom())
                       .map(dom -> dom.getElementType().getValue())
                       .orElse(null);
    }

    @Nullable
    @Override
    public TSMetaClassifier<? extends DomElement> getElementType() {
        final String typeName = getElementTypeName();
        return typeName == null
            ? null
            : TSMetaModelAccess.Companion.getInstance(getProject()).findMetaClassifierByName(typeName);
    }
}
