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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAttribute;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaIndex;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaSelfMerge;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaItemImpl extends TSMetaEntityImpl<ItemType> implements TSMetaItem {

    private final NoCaseMultiMap<TSMetaAttribute> myAttributes = new NoCaseMultiMap<>();
    private final NoCaseMultiMap<TSMetaCustomProperty> myCustomProperties = new NoCaseMultiMap<>();
    private final NoCaseMultiMap<TSMetaIndex> myIndexes = new NoCaseMultiMap<>();
    private final Set<DomAnchor<ItemType>> myAllDoms = new LinkedHashSet<>();

    private final String myTypeCode;
    private String myExtendedMetaItemName;

    public TSMetaItemImpl(final Project project, final String name, final String typeCode, final @NotNull ItemType dom) {
        super(project, name, dom);
        myTypeCode = typeCode;
        myAllDoms.add(DomService.getInstance().createAnchor(dom));
        registerExtends(dom);
    }

    @Override
    public String getTypeCode() {
        return myTypeCode;
    }

    @NotNull
    @Override
    public Stream<? extends ItemType> retrieveAllDomsStream() {
        return myAllDoms.stream()
                        .map(DomAnchor::retrieveDomElement)
                        .filter(Objects::nonNull);
    }

    @Override
    public void addAttribute(final String key, final TSMetaAttribute attribute) {
        myAttributes.putValue(key, attribute);
    }

    @Override
    public void addCustomProperty(final String key, final TSMetaCustomProperty customProperty) {
        myCustomProperties.putValue(key, customProperty);
    }

    @Override
    public void addIndex(final String key, final TSMetaIndex index) {
        myIndexes.putValue(key, index);
    }

    @Override
    public NoCaseMultiMap<TSMetaAttribute> getAttributes() {
        return myAttributes;
    }

    @Override
    public NoCaseMultiMap<TSMetaCustomProperty> getCustomAttributes() {
        return myCustomProperties;
    }

    @Override
    public NoCaseMultiMap<TSMetaIndex> getIndexes() {
        return myIndexes;
    }

    @Nullable
    @Override
    public String getExtendedMetaItemName() {
        return myExtendedMetaItemName;
    }

    @Override
    public void merge(final TSMetaSelfMerge<ItemType> another) {
        addDomRepresentation(another.retrieveDom());
    }

    protected void addDomRepresentation(final @NotNull ItemType anotherDom) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom));
        registerExtends(anotherDom);
    }

    private void registerExtends(final @NotNull ItemType dom) {
        //only one extends is allowed
        if (myExtendedMetaItemName == null) {
            myExtendedMetaItemName = dom.getExtends().getRawText();
        }
    }

}
