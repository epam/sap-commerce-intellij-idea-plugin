/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.meta.impl;

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TSMetaItemServiceImpl implements TSMetaItemService {

    private final Project myProject;

    public TSMetaItemServiceImpl(@NotNull final Project project) {
        myProject = project;
    }

    @Override
    public List<? extends TSGlobalMetaItem.TSGlobalMetaItemAttribute> findAttributesByName(final TSGlobalMetaItem meta, final String name, final boolean includeInherited) {
        return includeInherited
            ? Optional.ofNullable(meta.getAllAttributes().get(name))
                      .map(Collections::singletonList)
                      .orElseGet(Collections::emptyList)
            : Optional.ofNullable(meta.getAttributes().get(name))
                      .map(Collections::singletonList)
                      .orElseGet(Collections::emptyList);
    }

    @Override
    public Collection<? extends TSMetaRelation.TSMetaRelationElement> getRelationEnds(final TSGlobalMetaItem meta, final boolean includeInherited) {
        return includeInherited
            ? meta.getAllRelationEnds()
            : myProject.getService(TSMetaModelStateService.class).get().getRelations(meta.getName());
    }

    @Override
    public List<? extends TSMetaRelation.TSMetaRelationElement> findRelationEndsByQualifier(
        final TSGlobalMetaItem meta, @NotNull final String qualifier, final boolean includeInherited
    ) {
        return getRelationEnds(meta, includeInherited).stream()
                                                      .filter(ref -> qualifier.equalsIgnoreCase(ref.getQualifier()))
                                                      .collect(Collectors.toList());
    }

}
