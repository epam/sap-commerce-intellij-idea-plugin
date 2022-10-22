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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaMap;
import com.intellij.idea.plugin.hybris.type.system.model.MapType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class TSMetaMapImpl extends TSMetaEntityImpl<MapType> implements TSMetaMap {

    private final String myArgumentType;
    private final String myReturnType;
    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final boolean myRedeclare;
    private final Set<DomAnchor<MapType>> myAllDoms = new LinkedHashSet<>();

    public TSMetaMapImpl(final Module module, final Project project, final String name, final MapType dom, final boolean custom) {
        super(module, project, name, dom, custom);
        myArgumentType = dom.getArgumentType().getStringValue();
        myReturnType = dom.getReturnType().getStringValue();
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myRedeclare = Boolean.TRUE.equals(dom.getRedeclare().getValue());

        myAllDoms.add(DomService.getInstance().createAnchor(dom));
    }

    protected void addDomRepresentation(final @NotNull MapType anotherDom) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom));
    }

    @Override
    public @NotNull String getArgumentType() {
        return myArgumentType;
    }

    @Override
    public @NotNull String getReturnType() {
        return myReturnType;
    }

    @Override
    public @NotNull Stream<? extends MapType> retrieveAllDomsStream() {
        return myAllDoms.stream()
                        .map(DomAnchor::retrieveDomElement)
                        .filter(Objects::nonNull);
    }

    @Override
    public boolean isAutoCreate() {
        return myAutoCreate;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }

    @Override
    public boolean isRedeclare() {
        return myRedeclare;
    }

    @Override
    public void merge(final TSMetaMap another) {
        addDomRepresentation(another.retrieveDom());
    }
}
