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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation;
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

public class TSMetaItemServiceImpl implements TSMetaItemService {

    private final Project myProject;

    public TSMetaItemServiceImpl(@NotNull final Project project) {
        myProject = project;
    }

    @Override
    public Collection<? extends TSGlobalMetaItem.TSGlobalMetaItemAttribute> getAttributes(final TSGlobalMetaItem meta, final boolean includeInherited) {
        if (!includeInherited) {
            return meta.getAttributes().values();
        }

        return Optional.ofNullable(meta.getAllAttributes())
                       .orElseGet(() -> {
                           final List<TSGlobalMetaItem.TSGlobalMetaItemAttribute> result = new LinkedList<>();
                           final Consumer<TSGlobalMetaItem> visitor = parent -> result.addAll(parent.getAttributes().values());
                           walkInheritance(meta, visitor);
                           meta.setAllAttributes(result);

                           return result;
                       });
    }

    @Override
    public Collection<? extends TSMetaItem.TSMetaItemIndex> getIndexes(final TSGlobalMetaItem meta, final boolean includeInherited) {
        if (!includeInherited) {
            return meta.getIndexes().values();
        }
        return Optional.ofNullable(meta.getAllIndexes())
                       .orElseGet(() -> {
                           final List<TSGlobalMetaItem.TSGlobalMetaItemIndex> result = new LinkedList<>();
                           final Consumer<TSGlobalMetaItem> visitor = parent -> result.addAll(parent.getIndexes().values());
                           walkInheritance(meta, visitor);
                           meta.setAllIndexes(result);

                           return result;
                       });
    }

    @Override
    public Collection<? extends TSMetaCustomProperty> getCustomProperties(final TSGlobalMetaItem meta, final boolean includeInherited) {

        if (!includeInherited) {
            return meta.getCustomProperties().values();
        }

        return Optional.ofNullable(meta.getAllCustomProperties())
                       .orElseGet(() -> {
                           final List<TSMetaCustomProperty> result = new LinkedList<>();
                           final Consumer<TSGlobalMetaItem> visitor = parent -> result.addAll(parent.getCustomProperties().values());
                           walkInheritance(meta, visitor);
                           meta.setAllCustomProperties(result);

                           return result;
                       });
    }

    @Override
    public List<? extends TSGlobalMetaItem.TSGlobalMetaItemAttribute> findAttributesByName(final TSGlobalMetaItem meta, final String name, final boolean includeInherited) {
        if (!includeInherited) {
            final LinkedList<TSGlobalMetaItem.TSGlobalMetaItemAttribute> result = new LinkedList<>();
            collectOwnAttributesByName(meta, name, result);
            return result;
        }

        return getAttributes(meta, true).stream()
                                        .filter(attribute -> attribute.getName().equalsIgnoreCase(name))
                                        .collect(Collectors.toList());
    }

    @Override
    public Set<TSGlobalMetaItem> getExtends(final TSGlobalMetaItem meta) {
        if (meta.getAllExtends() != null) {
            return meta.getAllExtends();
        }

        final Set<TSGlobalMetaItem> tempParents = new LinkedHashSet<>();
        final Set<String> visitedParents = new HashSet<>();
        Optional<TSGlobalMetaItem> metaItem = getTsMetaItem(meta, visitedParents);

        while (metaItem.isPresent()) {
            tempParents.add(metaItem.get());
            metaItem = getTsMetaItem(metaItem.get(), visitedParents);
        }

        meta.setAllExtends(tempParents);
        return Collections.unmodifiableSet(tempParents);
    }

    @Override
    public List<? extends TSMetaRelation.TSMetaRelationElement> getRelationEnds(final TSGlobalMetaItem meta, final boolean includeInherited) {
        if (!includeInherited) {
            final LinkedList<TSMetaRelation.TSMetaRelationElement> result = new LinkedList<>();
            collectRelationForSourceType(meta, result);

            return result;
        }

        return Optional.ofNullable(meta.getAllRelationEnds())
            .orElseGet(() -> {
                final LinkedList<TSMetaRelation.TSMetaRelationElement> result = new LinkedList<>();
                final Consumer<TSGlobalMetaItem> visitor = mc -> collectRelationForSourceType(mc, result);
                walkInheritance(meta, visitor);

                meta.setAllRelationEnds(result);
                return result;
            });
    }

    @Override
    public List<? extends TSMetaRelation.TSMetaRelationElement> findReferenceEndsByQualifier(
        final TSGlobalMetaItem meta, @NotNull final String qualifier, final boolean includeInherited
    ) {
        return getRelationEnds(meta, includeInherited).stream()
                                                      .filter(ref -> ref.getQualifier().equalsIgnoreCase(qualifier))
                                                      .collect(Collectors.toList());
    }

    private Optional<TSGlobalMetaItem> getTsMetaItem(final TSGlobalMetaItem meta, final Set<String> visitedParents) {
        return Optional.of(getRealExtendedMetaItemName(meta))
                       .filter(aName -> !visitedParents.contains(aName))
                       .map(name -> TSMetaModelAccess.Companion.getInstance(myProject).findMetaItemByName(name));
    }

    private void collectOwnAttributesByName(final TSGlobalMetaItem meta, @NotNull final String name, @NotNull final Collection<TSGlobalMetaItem.TSGlobalMetaItemAttribute> output) {
        final var attr = meta.getAttributes().get(name);
        if (attr != null) {
            output.add(attr);
        }
    }

    /**
     * Iteratively applies given consumer for this class and all its super-classes.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void walkInheritance(final TSGlobalMetaItem meta, @NotNull final Consumer<TSGlobalMetaItem> visitor) {
        final Set<String> visited = new HashSet<>();
        visited.add(meta.getName());
        visitor.accept(meta);
        doWalkInheritance(meta, visited, visitor);
    }

    /**
     * Iteratively applies given consumer for inheritance chain, <strong>starting from the super-class</strong>.
     * Every super is visited only once, so this method takes care of inheritance cycles and rhombs
     */
    private void doWalkInheritance(final TSGlobalMetaItem meta, @NotNull final Set<String> visitedParents, @NotNull final Consumer<TSGlobalMetaItem> visitor) {
        Optional.of(getRealExtendedMetaItemName(meta))
                .filter(aName -> !visitedParents.contains(aName))
                .map(name -> TSMetaModelAccess.Companion.getInstance(myProject).findMetaItemByName(name))
                .ifPresent(parent -> {
                    visitedParents.add(parent.getName());
                    visitor.accept(parent);
                    doWalkInheritance(parent, visitedParents, visitor);
                });
    }

    private void collectRelationForSourceType(final TSMetaItem source, final LinkedList<TSMetaRelation.TSMetaRelationElement> out) {
        final var reference = TSMetaModelAccess.Companion.getInstance(myProject)
                                                         .getMetaModel()
                                                         .getReference(source.getName());
        if (reference != null) {
            out.add(reference);
        }
    }

    private String getRealExtendedMetaItemName(final TSGlobalMetaItem meta) {
        return meta.getExtendedMetaItemName() == null
            ? HybrisConstants.TS_IMPLICIT_SUPER_CLASS_NAME
            : meta.getExtendedMetaItemName();
    }
}
