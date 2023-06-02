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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ObjectFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.eclipse.EclipseProjectFinder;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
            return new ConfigHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, resolvedFile.getName());
        }

        if (hybrisProjectService.isCCv2Module(resolvedFile)) {
            LOG.info("Creating CCv2 module for " + path);
            return new CCv2HybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, resolvedFile.getName());
        }

        if (hybrisProjectService.isPlatformModule(resolvedFile)) {
            LOG.info("Creating Platform module for " + path);
            return new PlatformHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, HybrisConstants.EXTENSION_NAME_PLATFORM);
        }

        if (hybrisProjectService.isCoreExtModule(resolvedFile)) {
            LOG.info("Creating Core EXT module for " + path);
            return new CoreHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile));
        }

        if (hybrisProjectService.isPlatformExtModule(resolvedFile)) {
            LOG.info("Creating Platform EXT module for " + path);
            return new ExtHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile));
        }

        if (hybrisProjectService.isOutOfTheBoxModule(resolvedFile, rootProjectDescriptor)) {
            LOG.info("Creating OOTB module for " + path);
            return new OotbHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile));
        }

        if (hybrisProjectService.isHybrisModule(resolvedFile)) {
            LOG.info("Creating Custom hybris module for " + path);
            return new CustomHybrisModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile));
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
        return new EclipseModuleDescriptor(resolvedFile, rootProjectDescriptor, getEclipseModuleDescriptorName(resolvedFile));
    }

    private String getEclipseModuleDescriptorName(final File moduleRootDirectory) {
        String projectName = EclipseProjectFinder.findProjectName(moduleRootDirectory.getAbsolutePath());
        if (projectName != null) {
            projectName = projectName.trim();
            if (!projectName.isEmpty()) {
                return projectName;
            }
        }
        return moduleRootDirectory.getName();
    }

    @NotNull
    private ExtensionInfo getExtensionInfo(final File moduleRootDirectory) throws HybrisConfigurationException {
        final File hybrisProjectFile = new File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML);

        final ExtensionInfo extensionInfo = unmarshalExtensionInfo(hybrisProjectFile);

        if (null == extensionInfo.getExtension() || isBlank(extensionInfo.getExtension().getName())) {
            throw new HybrisConfigurationException("Can not find module name using path: " + moduleRootDirectory);
        }

        return extensionInfo;
    }

    @NotNull
    private ExtensionInfo unmarshalExtensionInfo(@NotNull final File hybrisProjectFile) throws HybrisConfigurationException {
        Validate.notNull(hybrisProjectFile);

        try {
            return (ExtensionInfo) JAXBContext.newInstance(
                                                  "com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo",
                                                  ObjectFactory.class.getClassLoader()
                                              )
                                              .createUnmarshaller()
                                              .unmarshal(hybrisProjectFile);
        } catch (final JAXBException e) {
            LOG.error("Can not unmarshal " + hybrisProjectFile.getAbsolutePath(), e);
            throw new HybrisConfigurationException("Can not unmarshal " + hybrisProjectFile);
        }
    }

}
