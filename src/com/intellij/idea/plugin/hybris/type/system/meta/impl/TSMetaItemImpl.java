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
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaSelfMerge;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaItemImpl extends TSMetaEntityImpl<ItemType> implements TSMetaItem {

    private final NoCaseMultiMap<TSMetaAttribute> myAttributes = new NoCaseMultiMap<>();

    private final Set<DomAnchor<ItemType>> myAllDoms = new LinkedHashSet<>();

    private final String myTypeCode;

    private String myExtendedMetaItemName = null;

    public TSMetaItemImpl(
        final Project project,
        final String name,
        final String typeCode,
        final @NotNull ItemType dom
    ) {
        super(project, name, dom);
        myTypeCode = typeCode;
        myAllDoms.add(DomService.getInstance().createAnchor(dom));
        registerExtends(dom);
    }

    @Override
    public String getTypeCode() {
        return myTypeCode;
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
    @NotNull
    public List<? extends TSMetaAttribute> getAttributes(final boolean includeInherited) {
        final LinkedList<TSMetaAttribute> result = new LinkedList<>();
        if (includeInherited) {
            walkInheritance(meta -> meta.collectOwnAttributes(result));
        } else {
            this.collectOwnAttributes(result);
        }
        return result;
    }

    private void collectOwnAttributes(@NotNull final Collection<TSMetaAttribute> output) {
        output.addAll(myAttributes.values());
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaAttribute> findAttributesByName(
        @NotNull final String name,
        final boolean includeInherited
    ) {
        final LinkedList<TSMetaAttribute> result = new LinkedList<>();
        if (includeInherited) {
            walkInheritance(meta -> meta.collectOwnAttributesByName(name, result));
        } else {
            this.collectOwnAttributesByName(name, result);
        }
        return result;
    }

    private void collectOwnAttributesByName(
        @NotNull final String name,
        @NotNull final Collection<TSMetaAttribute> output
    ) {
        output.addAll(myAttributes.get(name));
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaRelation.ReferenceEnd> getReferenceEndsStream(final boolean includeInherited) {
        final LinkedList<TSMetaRelation.ReferenceEnd> result = new LinkedList<>();
        final Consumer<TSMetaItemImpl> visitor = mc -> TSMetaModelService.Companion.getInstance(getProject()).collectReferencesForSourceType(mc, result);
        if (includeInherited) {
            walkInheritance(visitor);
        } else {
            visitor.accept(this);
        }
        return result.stream();
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaRelation.ReferenceEnd> findReferenceEndsByRole(
        @NotNull final String role, final boolean includeInherited
    ) {
        final String targetRoleNoCase = role.toLowerCase();
        return getReferenceEndsStream(includeInherited)
            .filter(ref -> ref.getRole().equalsIgnoreCase(targetRoleNoCase))
            .collect(Collectors.toList());
    }

    /**
     * Iteratively applies given consumer for this class and all its super-classes.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void walkInheritance(
        @NotNull final Consumer<TSMetaItemImpl> visitor
    ) {
        final Set<String> visited = new HashSet<>();
        visited.add(getName());
        visitor.accept(this);
        doWalkInheritance(visited, visitor);
    }

    /**
     * Iteratively applies given consumer for inheritance chain, <strong>starting from the super-class</strong>.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void doWalkInheritance(
        @NotNull final Set<String> visitedParents,
        @NotNull final Consumer<TSMetaItemImpl> visitor
    ) {
        Optional.of(getRealExtendedMetaItemName())
                .filter(aName -> !visitedParents.contains(aName))
                .map(name -> TSMetaModelService.Companion.getInstance(getProject()).findMetaItemByName(name))
                .filter(TSMetaItemImpl.class::isInstance)
                .map(TSMetaItemImpl.class::cast)
                .ifPresent(parent -> {
                    visitedParents.add(parent.getName());
                    visitor.accept(parent);
                    parent.doWalkInheritance(visitedParents, visitor);
                });
    }

    private String getRealExtendedMetaItemName() {
        return myExtendedMetaItemName == null ? IMPLICIT_SUPER_CLASS_NAME : myExtendedMetaItemName;
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
}
