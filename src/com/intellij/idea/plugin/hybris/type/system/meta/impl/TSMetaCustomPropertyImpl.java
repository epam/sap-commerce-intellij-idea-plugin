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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TSMetaCustomPropertyImpl extends TSMetaEntityImpl<CustomProperty> implements TSMetaCustomProperty {

    private final String myValue;

    public TSMetaCustomPropertyImpl(final Project project, final @NotNull CustomProperty dom) {
        super(project, extractName(dom), dom);
        myValue = dom.getValue().getRawText();
    }

    @Nullable
    @Override
    public String getName() {
        return super.getName();
    }

    @Nullable
    @Override
    public String getValue() {
        return myValue;
    }

    @Nullable
    private static String extractName(final CustomProperty dom) {
        return dom.getName().getStringValue();
    }

}
