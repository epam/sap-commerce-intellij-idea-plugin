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
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class TSMetaEnumImpl extends TSMetaEntityImpl<EnumType> implements TSMetaEnum {

    private final NoCaseMultiMap<TSMetaEnumValue> name2ValueObj = new NoCaseMultiMap<>();
    private final Set<DomAnchor<EnumType>> myAllDoms = new LinkedHashSet<>();
    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final boolean myDynamic;
    private final String myDescription;
    private final String myJaloClass;

    public TSMetaEnumImpl(final Module module, final Project project, final String name, final EnumType dom, final boolean custom) {
        super(module, project, name, dom, custom);
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myDynamic = Boolean.TRUE.equals(dom.getDynamic().getValue());
        myDescription = dom.getDescription().getStringValue();
        myJaloClass = dom.getJaloClass().getStringValue();

        myAllDoms.add(DomService.getInstance().createAnchor(dom));
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaEnumValue> getValues() {
        return name2ValueObj.values();
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaEnumValue> findValueByName(@NotNull final String name) {
        return new ArrayList<>(name2ValueObj.get(name));
    }

    @Override
    public void createValue(final @NotNull EnumValue domEnumValue) {
        final TSMetaEnumValue result = new TSMetaEnumValueImpl(getModule(), getProject(), this, domEnumValue, isCustom());

        if (result.getName() != null) {
            name2ValueObj.putValue(result.getName(), result);
        }
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
    public boolean isDynamic() {
        return myDynamic;
    }

    @Override
    public String getDescription() {
        return myDescription;
    }

    @Override
    public String getJaloClass() {
        return myJaloClass;
    }

    @Override
    public void merge(final TSMetaEnum another) {
        another.getValues().stream()
               .filter(anotherValue -> anotherValue.getName() != null)
               .forEach(anotherValue -> name2ValueObj.putValue(anotherValue.getName(), anotherValue));

        addDomRepresentation(another.retrieveDom());
    }

    protected void addDomRepresentation(final @NotNull EnumType anotherDom) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom));
    }

    public static class TSMetaEnumValueImpl extends TSMetaEntityImpl<EnumValue> implements TSMetaEnumValue {

        private final TSMetaEnum myOwner;
        private final String myDescription;

        public TSMetaEnumValueImpl(final Module module, final Project project, final @NotNull TSMetaEnum owner, final @NotNull EnumValue dom, final boolean custom) {
            super(module, project, extractEnumValueName(dom), dom, custom);
            myOwner = owner;
            myDescription = dom.getDescription().getStringValue();
        }

        @Nullable
        private static String extractEnumValueName(@NotNull final EnumValue dom) {
            return dom.getCode().getStringValue();
        }

        @Override
        public String getDescription() {
            return myDescription;
        }

        @NotNull
        @Override
        public TSMetaEnum getOwner() {
            return myOwner;
        }
    }
}
