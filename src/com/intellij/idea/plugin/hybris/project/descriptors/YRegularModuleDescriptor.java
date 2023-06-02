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

package com.intellij.idea.plugin.hybris.project.descriptors;

import com.intellij.idea.plugin.hybris.common.utils.CollectionUtils;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.MetaType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class YRegularModuleDescriptor extends AbstractYModuleDescriptor {

    @NotNull
    public final ExtensionInfo extensionInfo;
    public final Map<String, String> metas;
    private boolean inLocalExtensions;

    protected YRegularModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor,
        @NotNull final ExtensionInfo extensionInfo
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor, extensionInfo.getExtension().getName());

        this.extensionInfo = extensionInfo;
        this.metas = CollectionUtils.emptyListIfNull(extensionInfo.getExtension().getMeta()).stream()
            .collect(Collectors.toMap(MetaType::getKey, MetaType::getValue));
    }

    public boolean isInLocalExtensions() {
        return inLocalExtensions;
    }

    public void setInLocalExtensions(final boolean inLocalExtensions) {
        this.inLocalExtensions = inLocalExtensions;
    }

}
