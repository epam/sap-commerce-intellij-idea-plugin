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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.project.configurators.JavadocModuleConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavaModuleExternalPaths;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Aksenenko <serg.aksenenko@gmail.com> on 4/6/2016.
 */
public class DefaultJavadocModuleConfigurator implements JavadocModuleConfigurator {

    @Override
    public void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ProgressIndicator progressIndicator
    ) {
        Validate.notNull(modifiableRootModel);
        Validate.notNull(moduleDescriptor);

        final String javadocUrl = moduleDescriptor.getRootProjectDescriptor().getJavadocUrl();

        final List<String> javadocPathList = MavenUtils.resolveMavenJavadocs(modifiableRootModel, moduleDescriptor, progressIndicator);
        final JavaModuleExternalPaths javaModuleExternalPaths = modifiableRootModel.getModuleExtension(
            JavaModuleExternalPaths.class
        );

        final List<String> javadocRefList = new ArrayList<>();
        for (final String javadocPath : javadocPathList) {
            javadocRefList.add("jar://" + javadocPath + "!/");
        }
        javadocRefList.sort(String::compareTo);

        if (null != javadocUrl) {
            javadocRefList.add(javadocUrl);
        }

        javaModuleExternalPaths.setJavadocUrls(javadocRefList.toArray(new String[0]));
    }
}
