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

import com.intellij.facet.ModifiableFacetModel;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.SpringConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.*;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YConfigModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCoreExtRegularModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformModuleDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFileSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.io.FileUtilRt.toSystemDependentName;

public class DefaultSpringConfigurator implements SpringConfigurator {

    private static final Logger LOG = Logger.getInstance(DefaultSpringConfigurator.class);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" ,");

    @Override
    public void findSpringConfiguration(@NotNull final List<ModuleDescriptor> modulesChosenForImport) {
        Validate.notNull(modulesChosenForImport);

        final var yModuleDescriptors = modulesChosenForImport.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .toList();

        final Map<String, YModuleDescriptor> moduleDescriptorMap = new HashMap<>();
        File localProperties = null;
        File advancedProperties = null;
        for (YModuleDescriptor moduleDescriptor : yModuleDescriptors) {
            moduleDescriptorMap.put(moduleDescriptor.getName(), moduleDescriptor);
            if (moduleDescriptor instanceof final YConfigModuleDescriptor configModule) {
                localProperties = new File(configModule.getRootDirectory(), HybrisConstants.LOCAL_PROPERTIES);
            }
            if (moduleDescriptor instanceof final YPlatformModuleDescriptor platformModule) {
                advancedProperties = new File(platformModule.getRootDirectory(), HybrisConstants.ADVANCED_PROPERTIES);
            }
        }
        for (YModuleDescriptor moduleDescriptor : yModuleDescriptors) {
            processHybrisModule(moduleDescriptorMap, moduleDescriptor);
            if (moduleDescriptor instanceof YCoreExtRegularModuleDescriptor) {
                if (advancedProperties != null) {
                    moduleDescriptor.addSpringFile(advancedProperties.getAbsolutePath());
                }
                if (localProperties != null) {
                    moduleDescriptor.addSpringFile(localProperties.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void configureDependencies(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final Map<String, ModifiableFacetModel> modifiableFacetModelMap = new HashMap<>();

        for (Module module : modifiableModelsProvider.getModules()) {
            final ModifiableFacetModel modifiableFacetModel = modifiableModelsProvider.getModifiableFacetModel(module);
            modifiableFacetModelMap.put(module.getName(), modifiableFacetModel);
        }

        hybrisProjectDescriptor.getModulesChosenForImport().stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .forEach(it ->  configureFacetDependencies(it, modifiableFacetModelMap));
    }

    private void configureFacetDependencies(
        @NotNull final YModuleDescriptor moduleDescriptor,
        @NotNull final Map<String, ModifiableFacetModel> modifiableFacetModelMap
    ) {
        Validate.notNull(moduleDescriptor);
        Validate.notNull(modifiableFacetModelMap);

        final SpringFileSet springFileSet = getSpringFileSet(modifiableFacetModelMap, moduleDescriptor.getName());
        if (springFileSet == null) {
            return;
        }

        final Set<YModuleDescriptor> dependenciesTree = moduleDescriptor.getDependenciesTree();
        final List<YModuleDescriptor> sortedDependenciesTree = dependenciesTree.stream()
            .sorted()
            .toList();
        for (YModuleDescriptor dependsOnModule : sortedDependenciesTree) {
            final SpringFileSet parentFileSet = getSpringFileSet(modifiableFacetModelMap, dependsOnModule.getName());
            if (parentFileSet == null) {
                continue;
            }
            springFileSet.addDependency(parentFileSet);
        }
    }

    private SpringFileSet getSpringFileSet(
        @NotNull final Map<String, ModifiableFacetModel> modifiableFacetModelMap,
        @NotNull final String moduleName
    ) {
        Validate.notNull(moduleName);
        Validate.notNull(modifiableFacetModelMap);

        final ModifiableFacetModel modifiableFacetModel = modifiableFacetModelMap.get(moduleName);
        if (modifiableFacetModel == null) {
            return null;
        }
        final SpringFacet springFacet = modifiableFacetModel.getFacetByType(SpringFacet.FACET_TYPE_ID);
        if (springFacet == null || springFacet.getFileSets().isEmpty()) {
            return null;
        }
        return springFacet.getFileSets().iterator().next();
    }

    private void processHybrisModule(
        @NotNull final Map<String, YModuleDescriptor> moduleDescriptorMap,
        @NotNull final YModuleDescriptor moduleDescriptor
    ) {
        Validate.notNull(moduleDescriptorMap);
        Validate.notNull(moduleDescriptor);

        processPropertiesFile(moduleDescriptorMap, moduleDescriptor);
        try {
            processWebXml(moduleDescriptorMap, moduleDescriptor);
        } catch (Exception e) {
            LOG.error("Unable to parse web.xml for module "+moduleDescriptor.getName(), e);
        }
        try {
            processBackofficeXml(moduleDescriptorMap, moduleDescriptor);
        } catch (Exception e) {
            LOG.error("Unable to parse web.xml for module "+moduleDescriptor.getName(), e);
        }
    }

    private void processPropertiesFile(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor
    ) {
        final Properties projectProperties = new Properties();

        final File propFile = new File(moduleDescriptor.getRootDirectory(), HybrisConstants.PROJECT_PROPERTIES);
        moduleDescriptor.addSpringFile(propFile.getAbsolutePath());

        try (final FileInputStream fis = new FileInputStream(propFile)) {
            projectProperties.load(fis);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            LOG.error("", e);
            return;
        }

        for (String key : projectProperties.stringPropertyNames()) {
            if (key.endsWith(HybrisConstants.APPLICATION_CONTEXT_SPRING_FILES)
                || key.endsWith(HybrisConstants.ADDITIONAL_WEB_SPRING_CONFIG_FILES)
                || key.endsWith(HybrisConstants.GLOBAL_CONTEXT_SPRING_FILES)) {
                final String moduleName = key.substring(0, key.indexOf('.'));
                // relevantModule can be different to a moduleDescriptor. e.g. addon concept
                final YModuleDescriptor relevantModule = moduleDescriptorMap.get(moduleName);
                if (relevantModule != null) {
                    final String rawFile = projectProperties.getProperty(key);
                    if (rawFile == null) {
                        continue;
                    }
                    for (String file : rawFile.split(",")) {
                        if (!addSpringXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(relevantModule), file)) {
                            final File dir = hackGuessLocation(relevantModule);
                            addSpringXmlFile(moduleDescriptorMap, relevantModule, dir, file);
                        }
                    }
                }
            }
        }
    }

    // This is not a nice practice but the platform has a bug in acceleratorstorefrontcommons/project.properties.
    // See https://jira.hybris.com/browse/ECP-3167
    private File hackGuessLocation(final YModuleDescriptor moduleDescriptor) {
        return new File(getResourceDir(moduleDescriptor), toSystemDependentName(moduleDescriptor.getName() + "/web/spring"));
    }

    private void processBackofficeXml(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor
    ) {
        final var files = new File(moduleDescriptor.getRootDirectory(), HybrisConstants.RESOURCES_DIRECTORY)
            .listFiles((dir, name) -> name.endsWith("-backoffice-spring.xml"));

        if (files == null || files.length == 0) return;

        Arrays.stream(files)
            .forEach(file -> processSpringFile(moduleDescriptorMap, moduleDescriptor, file));
    }

    private void processWebXml(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor
    ) throws IOException, JDOMException {
        final File webXml = new File(moduleDescriptor.getRootDirectory(), HybrisConstants.WEB_XML_DIRECTORY_RELATIVE_PATH);
        if (!webXml.exists()) {
            return;
        }
        final Element root = getDocumentRoot(webXml);
        if (root.isEmpty() || !root.getName().equals("web-app")) {
            return;
        }
        final String location = root.getChildren()
                                    .stream()
                                    .filter(e -> e.getName().equals("context-param"))
                                    .filter(e -> e.getChildren().stream().anyMatch(p -> p.getName().equals("param-name") && p.getValue().equals("contextConfigLocation")))
                                    .map(e -> e.getChildren().stream().filter(p -> p.getName().equals("param-value")).findFirst().orElse(null))
                                    .filter(Objects::nonNull)
                                    .map(Element::getValue)
                                    .findFirst().orElse(null);
        if (location == null) {
            return;
        }
        processContextParam(moduleDescriptorMap, moduleDescriptor, location.trim());
    }

    private void processContextParam(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor,
        final String contextConfigLocation
    ) {
        final File webModuleDir = new File(moduleDescriptor.getRootDirectory(), HybrisConstants.WEB_ROOT_DIRECTORY_RELATIVE_PATH);
        for (String xml: SPLIT_PATTERN.split(contextConfigLocation)) {
            if (!xml.endsWith(".xml")) {
                continue;
            }
            final File springFile = new File(webModuleDir, xml);
            if (!springFile.exists()) {
                continue;
            }
            processSpringFile(moduleDescriptorMap, moduleDescriptor, springFile);
        }
    }

    private @NotNull Element getDocumentRoot(final File inputFile) throws IOException, JDOMException {
        return JDOMUtil.load(inputFile);
    }

    private boolean hasSpringContent(final File springFile) throws IOException, JDOMException {
        final Element root = getDocumentRoot(springFile);
        return !root.isEmpty() && root.getName().equals("beans");
    }

    private boolean processSpringFile(final Map<String, YModuleDescriptor> moduleDescriptorMap,
                                      final YModuleDescriptor relevantModule,
                                      final File springFile) {
        try {
            if (!hasSpringContent(springFile)) {
                return false;
            }
            if (relevantModule.addSpringFile(springFile.getAbsolutePath())) {
                scanForSpringImport(moduleDescriptorMap, relevantModule, springFile);
            }
            return true;
        } catch (Exception e) {
            LOG.error("unable scan file for spring imports "+springFile.getName());
        }
        return false;
    }

    private void scanForSpringImport(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor,
        final File springFile
    ) throws IOException, JDOMException {
        final Element root = getDocumentRoot(springFile);
        final List<Element> importList = root.getChildren().stream()
                                             .filter(e -> e.getName().equals("import"))
                                             .toList();
        processImportNodeList(moduleDescriptorMap, moduleDescriptor, importList, springFile);
    }

    private void processImportNodeList(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor moduleDescriptor,
        final List<Element> importList,
        final File springFile
    ) {
        for (Element importElement: importList) {
            final String resource = importElement.getAttributeValue("resource");
            if (resource.startsWith("classpath:")) {
                addSpringOnClasspath(moduleDescriptorMap, moduleDescriptor, resource.substring("classpath:".length()));
            } else {
                addSpringXmlFile(moduleDescriptorMap, moduleDescriptor, springFile.getParentFile(), resource);
            }
        }
    }
    private void addSpringOnClasspath(
        final Map<String, YModuleDescriptor> moduleDescriptorMap,
        final YModuleDescriptor relevantModule,
        final String fileOnClasspath
    ) {
        if (addSpringXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(relevantModule), fileOnClasspath)) {
            return;
        }
        final String file = StringUtils.stripStart(fileOnClasspath, "/");
        final int index = file.indexOf("/");
        if (index != -1) {
            final String moduleName = file.substring(0, index);
            final YModuleDescriptor module = moduleDescriptorMap.get(moduleName);
            if (module != null && addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(module), fileOnClasspath)) {
                return;
            }
        }
        moduleDescriptorMap.values().stream().anyMatch(module->addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(module), fileOnClasspath));
    }

    private boolean addSpringXmlFile(final Map<String, YModuleDescriptor> moduleDescriptorMap,
                                     final YModuleDescriptor moduleDescriptor,
                                     final File resourceDirectory,
                                     final String file) {
        if (StringUtils.startsWith(file, "/")) {
            return addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, getResourceDir(moduleDescriptor), file);
        }
        return addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, resourceDirectory, file);
    }

    private File getResourceDir(final YModuleDescriptor moduleToSearch) {
        return new File(moduleToSearch.getRootDirectory(), HybrisConstants.RESOURCES_DIRECTORY);
    }

    private boolean addSpringExternalXmlFile(final Map<String, YModuleDescriptor> moduleDescriptorMap,
                                             final YModuleDescriptor moduleDescriptor,
                                             final File resourcesDir,
                                             final String file) {
        final File springFile = new File(resourcesDir, file);
        if (!springFile.exists()) {
            return false;
        }
        return processSpringFile(moduleDescriptorMap, moduleDescriptor, springFile);
    }

}
