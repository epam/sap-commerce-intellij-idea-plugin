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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaClassImpl extends TSMetaEntityImpl<ItemType> implements TSMetaClass {

    private final NoCaseMultiMap<TSMetaProperty> myProperties = new NoCaseMultiMap<>();

    private final Set<DomAnchor<ItemType>> myAllDoms = new LinkedHashSet<>();

    private final String myTypeCode;

    private String myExtendedMetaClassName = null;

    public TSMetaClassImpl(
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

    public void addDomRepresentation(final @NotNull ItemType anotherDom) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom));
        registerExtends(anotherDom);
    }

    private void registerExtends(final @NotNull ItemType dom) {
        //only one extends is allowed
        if (myExtendedMetaClassName == null) {
            myExtendedMetaClassName = dom.getExtends().getRawText();
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
    public void addProperty(final String key, final TSMetaProperty property) {
        myProperties.putValue(key, property);
    }

    @NotNull
    @Override
    public String getName() {
        //noinspection NullableProblems
        return super.getName();
    }

    @Override
    @NotNull
    public Stream<? extends TSMetaProperty> getPropertiesStream(final boolean includeInherited) {
        final LinkedList<TSMetaProperty> result = new LinkedList<>();
        if (includeInherited) {
            walkInheritance(meta -> meta.collectOwnProperties(result));
        } else {
            this.collectOwnProperties(result);
        }
        return result.stream();
    }

    private void collectOwnProperties(@NotNull final Collection<TSMetaProperty> output) {
        output.addAll(myProperties.values());
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaProperty> findPropertiesByName(
        @NotNull final String name,
        final boolean includeInherited
    ) {
        final LinkedList<TSMetaProperty> result = new LinkedList<>();
        if (includeInherited) {
            walkInheritance(meta -> meta.collectOwnPropertiesByName(name, result));
        } else {
            this.collectOwnPropertiesByName(name, result);
        }
        return result;
    }

    private void collectOwnPropertiesByName(
        @NotNull final String name,
        @NotNull final Collection<TSMetaProperty> output
    ) {
        output.addAll(myProperties.get(name));
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaReference.ReferenceEnd> getReferenceEndsStream(final boolean includeInherited) {
        final LinkedList<TSMetaReference.ReferenceEnd> result = new LinkedList<>();
        final Consumer<TSMetaClassImpl> visitor = mc -> TSMetaModelService.Companion.getInstance(getProject()).collectReferencesForSourceType(mc, result);
        if (includeInherited) {
            walkInheritance(visitor);
        } else {
            visitor.accept(this);
        }
        return result.stream();
    }

    @NotNull
    @Override
    public Collection<? extends TSMetaReference.ReferenceEnd> findReferenceEndsByRole(
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
        @NotNull final Consumer<TSMetaClassImpl> visitor
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
        @NotNull final Consumer<TSMetaClassImpl> visitor
    ) {
        Optional.ofNullable(getRealExtendedMetaClassName())
                .filter(aName -> !visitedParents.contains(aName))
                .map(name -> TSMetaModelService.Companion.getInstance(getProject()).findMetaClassByName(name))
                .filter(TSMetaClassImpl.class::isInstance)
                .map(TSMetaClassImpl.class::cast)
                .ifPresent(parent -> {
                    visitedParents.add(parent.getName());
                    visitor.accept(parent);
                    parent.doWalkInheritance(visitedParents, visitor);
                });
    }

    private String getRealExtendedMetaClassName() {
        return myExtendedMetaClassName == null ? IMPLICIT_SUPER_CLASS_NAME : myExtendedMetaClassName;
    }

    @Nullable
    @Override
    public String getExtendedMetaClassName() {
        return myExtendedMetaClassName;
    }

}
