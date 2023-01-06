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

import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created 1:58 PM 20 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultHybrisModuleDescriptorFactory implements HybrisModuleDescriptorFactory {

    private static final Logger LOG = Logger.getInstance(DefaultHybrisModuleDescriptorFactory.class);

    @NotNull
    @Override
    public HybrisModuleDescriptor createDescriptor(
        @NotNull final File file,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor
    ) throws HybrisConfigurationException {
        Validate.notNull(file);
        Validate.notNull(rootProjectDescriptor);

        final HybrisProjectService hybrisProjectService = ApplicationManager.getApplication().getService(HybrisProjectService.class);

        String path = file.getAbsolutePath();
        final File resolvedFile;
        try {
            resolvedFile = file.getCanonicalFile();
        } catch (IOException e) {
            throw new HybrisConfigurationException(e);
        }
        final String newPath = resolvedFile.getAbsolutePath();
        if (!path.equals(newPath)) {
            path = path + '(' + newPath + ')';
        }
        if (hybrisProjectService.isConfigModule(resolvedFile)) {
            LOG.info("Creating Config module for " + path);
            return new ConfigHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isPlatformModule(resolvedFile)) {
            LOG.info("Creating Platform module for " + path);
            return new PlatformHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isCoreExtModule(resolvedFile)) {
            LOG.info("Creating Core EXT module for " + path);
            return new CoreHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isPlatformExtModule(resolvedFile)) {
            LOG.info("Creating Platform EXT module for " + path);
            return new ExtHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isOutOfTheBoxModule(resolvedFile, rootProjectDescriptor)) {
            LOG.info("Creating OOTB module for " + path);
            return new OotbHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isHybrisModule(resolvedFile)) {
            LOG.info("Creating Custom hybris module for " + path);
            return new CustomHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isGradleModule(resolvedFile)) {
            LOG.info("Creating gradle module for " + path);
            return new GradleModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        if (hybrisProjectService.isMavenModule(resolvedFile)) {
            LOG.info("Creating maven module for " + path);
            return new MavenModuleDescriptor(resolvedFile, rootProjectDescriptor);
        }

        LOG.info("Creating eclipse module for " + path);
        return new EclipseModuleDescriptor(resolvedFile, rootProjectDescriptor);
    }

}
