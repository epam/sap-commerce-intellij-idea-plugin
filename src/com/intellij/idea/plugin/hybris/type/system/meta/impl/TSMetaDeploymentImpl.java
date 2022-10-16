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
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaDeployment;
import com.intellij.idea.plugin.hybris.type.system.model.Deployment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TSMetaDeploymentImpl<T extends TSMetaClassifier<?>> extends TSMetaEntityImpl<Deployment> implements TSMetaDeployment<T> {

    private final T myOwner;
    private final String myPropertyTable;
    private final String myTypeCode;

    public TSMetaDeploymentImpl(final Project project, final @NotNull T owner, final @NotNull Deployment dom) {
        super(project, extractName(dom), dom);
        myOwner = owner;
        myTypeCode = dom.getTypeCode().getStringValue();
        myPropertyTable = dom.getPropertyTable().getStringValue();
    }

    @Nullable
    @Override
    public String getName() {
        return super.getName();
    }

    @Nullable
    @Override
    public String getTable() {
        return getName();
    }

    @Nullable
    @Override
    public String getPropertyTable() {
        return myPropertyTable;
    }

    @Nullable
    @Override
    public String getTypeCode() {
        return myTypeCode;
    }

    @Override
    public T getOwner() {
        return myOwner;
    }

    private static String extractName(final Deployment dom) {
        return dom.getTable().getStringValue();
    }
}
