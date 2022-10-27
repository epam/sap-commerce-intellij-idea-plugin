/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.meta.model.impl.TSMetaItemImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TSMetaItemServiceImpl implements TSMetaItemService {

    private final Project myProject;

    public TSMetaItemServiceImpl(@NotNull final Project project) {
        myProject = project;
    }

    @Override
    public List<? extends TSMetaItem.TSMetaItemAttribute> getAttributes(final TSMetaItem meta, final boolean includeInherited) {
        final List<TSMetaItem.TSMetaItemAttribute> result = new LinkedList<>();
        if (includeInherited) {
            final Consumer<TSMetaItem> visitor = parent -> collectOwnAttributes(parent, result);
            walkInheritance(meta, visitor);
        } else {
            collectOwnAttributes(meta, result);
        }
        return result;
    }

    @Override
    public List<? extends TSMetaItem.TSMetaItemIndex> getIndexes(final TSMetaItem meta, final boolean includeInherited) {
        final List<TSMetaItem.TSMetaItemIndex> result = new LinkedList<>();
        if (includeInherited) {
            final Consumer<TSMetaItem> visitor = parent -> collectOwnIndexes(parent, result);
            walkInheritance(meta, visitor);
        } else {
            collectOwnIndexes(meta, result);
        }
        return result;
    }

    @Override
    public @NotNull List<? extends TSMetaCustomProperty> getCustomProperties(final TSMetaItem meta, final boolean includeInherited) {
        final List<TSMetaCustomProperty> result = new LinkedList<>();
        if (includeInherited) {
            final Consumer<TSMetaItem> visitor = parent -> collectOwnCustomProperties(parent, result);
            walkInheritance(meta, visitor);
        } else {
            collectOwnCustomProperties(meta, result);
        }
        return result;
    }

    @Override
    public Collection<? extends TSMetaItem.TSMetaItemAttribute> findAttributesByName(final TSMetaItem meta, final String name, final boolean includeInherited) {
        final LinkedList<TSMetaItem.TSMetaItemAttribute> result = new LinkedList<>();
        if (includeInherited) {
            final Consumer<TSMetaItem> visitor = parentMeta -> collectOwnAttributesByName(parentMeta, name, result);
            walkInheritance(meta, visitor);
        } else {
            collectOwnAttributesByName(meta, name, result);
        }
        return result;
    }

    @Override
    public Set<TSMetaItem> getExtends(final TSMetaItem meta) {
        final Set<TSMetaItem> tempParents = new LinkedHashSet<>();
        final Set<String> visitedParents = new HashSet<>();
        Optional<TSMetaItem> metaItem = getTsMetaItem(meta, visitedParents);

        while (metaItem.isPresent()) {
            tempParents.add(metaItem.get());
            metaItem = getTsMetaItem(metaItem.get(), visitedParents);
        }

        return Collections.unmodifiableSet(tempParents);
    }

    @Override
    public Stream<? extends TSMetaRelation.TSMetaRelationElement> getReferenceEndsStream(final TSMetaItem meta, final boolean includeInherited) {
        final LinkedList<TSMetaRelation.TSMetaRelationElement> result = new LinkedList<>();
        final Consumer<TSMetaItem> visitor = mc -> TSMetaModelAccess.Companion.getInstance(myProject).collectReferencesForSourceType(mc, result);
        if (includeInherited) {
            walkInheritance(meta, visitor);
        } else {
            visitor.accept(meta);
        }
        return result.stream();
    }

    @Override
    public Collection<? extends TSMetaRelation.TSMetaRelationElement> findReferenceEndsByRole(
        final TSMetaItem meta, @NotNull final String role, final boolean includeInherited
    ) {
        final String targetRoleNoCase = role.toLowerCase();
        return getReferenceEndsStream(meta, includeInherited)
            .filter(ref -> ref.getQualifier().equalsIgnoreCase(targetRoleNoCase))
            .collect(Collectors.toList());
    }

    private Optional<TSMetaItem> getTsMetaItem(final TSMetaItem meta, final Set<String> visitedParents) {
        return Optional.of(getRealExtendedMetaItemName(meta))
                       .filter(aName -> !visitedParents.contains(aName))
                       .map(name -> TSMetaModelAccess.Companion.getInstance(myProject).findMetaItemByName(name));
    }

    private void collectOwnAttributes(final TSMetaItem meta, @NotNull final Collection<TSMetaItem.TSMetaItemAttribute> output) {
        output.addAll(meta.getAttributes().values());
    }

    private void collectOwnIndexes(final TSMetaItem meta, @NotNull final Collection<TSMetaItem.TSMetaItemIndex> output) {
        output.addAll(meta.getIndexes().values());
    }

    private void collectOwnCustomProperties(final TSMetaItem meta, @NotNull final Collection<TSMetaCustomProperty> output) {
        output.addAll(meta.getCustomProperties().values());
    }

    private void collectOwnAttributesByName(final TSMetaItem meta, @NotNull final String name, @NotNull final Collection<TSMetaItem.TSMetaItemAttribute> output) {
        output.addAll(meta.getAttributes().get(name));
    }

    /**
     * Iteratively applies given consumer for this class and all its super-classes.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void walkInheritance(final TSMetaItem meta, @NotNull final Consumer<TSMetaItem> visitor) {
        final Set<String> visited = new HashSet<>();
        visited.add(meta.getName());
        visitor.accept(meta);
        doWalkInheritance(meta, visited, visitor);
    }

    /**
     * Iteratively applies given consumer for inheritance chain, <strong>starting from the super-class</strong>.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void doWalkInheritance(final TSMetaItem meta, @NotNull final Set<String> visitedParents, @NotNull final Consumer<TSMetaItem> visitor) {
        Optional.of(getRealExtendedMetaItemName(meta))
                .filter(aName -> !visitedParents.contains(aName))
                .map(name -> TSMetaModelAccess.Companion.getInstance(myProject).findMetaItemByName(name))
                .filter(TSMetaItemImpl.class::isInstance)
                .map(TSMetaItemImpl.class::cast)
                .ifPresent(parent -> {
                    visitedParents.add(parent.getName());
                    visitor.accept(parent);
                    doWalkInheritance(parent, visitedParents, visitor);
                });
    }

    private String getRealExtendedMetaItemName(final TSMetaItem meta) {
        return meta.getExtendedMetaItemName() == null
            ? TSMetaItem.IMPLICIT_SUPER_CLASS_NAME
            : meta.getExtendedMetaItemName();
    }
}
