/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class DefaultJavadocModuleConfigurator implements JavadocModuleConfigurator {

    @Override
    public void configure(
        @NotNull ProgressIndicator indicator,
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        indicator.setText2(message("hybris.project.import.module.javadoc"));

        final String javadocUrl = moduleDescriptor.getRootProjectDescriptor().getJavadocUrl();

        final List<String> javadocPathList = MavenUtils.resolveMavenJavadocs(modifiableRootModel, moduleDescriptor, indicator);
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
