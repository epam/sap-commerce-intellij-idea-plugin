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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaIndex;
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode;
import com.intellij.idea.plugin.hybris.type.system.model.Index;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TSMetaIndexImpl extends TSMetaEntityImpl<Index> implements TSMetaIndex {

    private final boolean myRemove;
    private final boolean myReplace;
    private final boolean myUnique;
    private final Set<String> myKeys;
    private final CreationMode myCreationMode;

    public TSMetaIndexImpl(final Project project, final @NotNull Index dom) {
        super(project, extractName(dom), dom);
        myRemove = Boolean.TRUE.equals(dom.getRemove().getValue());
        myReplace = Boolean.TRUE.equals(dom.getReplace().getValue());
        myUnique = Boolean.TRUE.equals(dom.getUnique().getValue());
        myCreationMode = Optional.ofNullable(dom.getCreationMode().getValue()).orElse(CreationMode.ALL);
        myKeys = dom.getKeys().stream()
                    .map(indexKey -> indexKey.getAttribute().getStringValue())
                    .collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(myKeys);
    }

    @Nullable
    private static String extractName(final Index dom) {
        return dom.getName().getStringValue();
    }

    @Override
    public boolean isRemove() {
        return myRemove;
    }

    @Override
    public boolean isReplace() {
        return myReplace;
    }

    @Override
    public boolean isUnique() {
        return myUnique;
    }

    @Override
    public CreationMode getCreationMode() {
        return myCreationMode;
    }
}
