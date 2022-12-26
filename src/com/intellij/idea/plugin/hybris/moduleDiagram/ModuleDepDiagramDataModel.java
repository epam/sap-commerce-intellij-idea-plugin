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

package com.intellij.idea.plugin.hybris.moduleDiagram;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramEdge;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.VisibilityLevel;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.uml.java.project.ModuleItem;
import com.intellij.uml.java.project.ModulesUmlEdge;
import com.intellij.uml.java.project.ModulesUmlNode;
import com.intellij.uml.java.project.ModulesUmlProvider;
import com.intellij.uml.java.project.UmlModulesRelationshipHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Eugene.Kudelevsky
 */
@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
public class ModuleDepDiagramDataModel extends DiagramDataModel<ModuleDepDiagramItem> {

    @NotNull
    private final Set<ModuleDepDiagramNode> myNodes = new HashSet<>();
    @NotNull
    private final Set<ModuleDepDiagramEdge> myEdges = new HashSet<>();

    public ModuleDepDiagramDataModel(final Project project, final DiagramProvider<ModuleDepDiagramItem> provider) {
        super(project, provider);
    }

    @NotNull
    @Override
    public Collection<? extends DiagramNode<ModuleDepDiagramItem>> getNodes() {
        return new ArrayList<>(myNodes);
    }

    @NotNull
    @Override
    public Collection<? extends DiagramEdge<ModuleDepDiagramItem>> getEdges() {
        return new ArrayList<>(myEdges);
    }

    @NotNull
    @Override
    public String getNodeName(final DiagramNode<ModuleDepDiagramItem> node) {
        return node.getIdentifyingElement().getName();
    }

    @Nullable
    @Override
    public DiagramNode<ModuleDepDiagramItem> addElement(final ModuleDepDiagramItem moduleItem) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshDataModel() {
        new MyRefresher().refresh();
    }

    @NotNull
    @Override
    public ModificationTracker getModificationTracker() {
        //noinspection ReturnOfThis
        return this;
    }

    @Override
    public void dispose() {
    }

    private class MyRefresher {

        private final ModuleManager myModuleManager = ModuleManager.getInstance(getProject());

        public void refresh() {
            myNodes.clear();
            myEdges.clear();

            final List<ModuleItem> items = getModulesToShow()
                .stream()
                .map(ModuleItem::new)
                .collect(Collectors.toList());

            final ModulesUmlProvider modulesUmlProvider = new ModulesUmlProvider();

            final Map<ModuleItem, ModulesUmlNode> item2Node = items.stream().collect(Collectors.toMap(
                Function.identity(), item -> new ModulesUmlNode(item, modulesUmlProvider)));

            final List<ModulesUmlEdge> edges = UmlModulesRelationshipHelper.generateEdges(
                item2Node, getProject(), false);

            myNodes.addAll(createAdaptedNodes(items));
            myEdges.addAll(createAdaptedEdges(edges));
            new TarjanCircularDetection(myNodes, myEdges).detectAndMarkCircles();
        }

        @NotNull
        private Collection<Module> getModulesToShow() {
            final VisibilityLevel visibilityLevel = getVisibilityManager().getCurrentVisibilityLevel();
            final Module[] allModules = myModuleManager.getModules();

            if (ModuleDepDiagramVisibilityManager.LARGE.equals(visibilityLevel)) {
                return Arrays
                    .stream(allModules)
                    .filter(module -> isCustomExtension(module) || isOotbOrPlatformExtension(module))
                    .collect(Collectors.toList());
            }
            final List<Module> customExtModules = Arrays
                .stream(allModules)
                .filter(this::isCustomExtension)
                .collect(Collectors.toList());

            if (ModuleDepDiagramVisibilityManager.SMALL.equals(visibilityLevel)) {
                return customExtModules;
            }
            final List<Module> dependencies = customExtModules
                .stream()
                .flatMap(module -> Arrays.stream(ModuleRootManager.getInstance(module).getDependencies()))
                .filter(this::isOotbOrPlatformExtension)
                .toList();

            final List<Module> backwardDependencies = Arrays
                .stream(allModules)
                .filter(module -> Arrays
                    .stream(ModuleRootManager.getInstance(module).getDependencies())
                    .anyMatch(this::isCustomExtension))
                .filter(this::isOotbOrPlatformExtension)
                .toList();

            final Set<Module> result = new HashSet<>();
            result.addAll(customExtModules);
            result.addAll(dependencies);
            result.addAll(backwardDependencies);
            return result;
        }

        private boolean isCustomExtension(@NotNull final Module module) {
            return HybrisModuleDescriptor.getDescriptorType(module) == HybrisModuleDescriptorType.CUSTOM;
        }

        private boolean isOotbOrPlatformExtension(@NotNull final Module module) {
            final HybrisModuleDescriptorType descriptorType = HybrisModuleDescriptor.getDescriptorType(module);
            return descriptorType == HybrisModuleDescriptorType.OOTB || descriptorType == HybrisModuleDescriptorType.PLATFORM;
        }

        @SuppressWarnings("unchecked")
        @NotNull
        private List<ModuleDepDiagramEdge> createAdaptedEdges(@NotNull final Collection<ModulesUmlEdge> edges) {
            final DiagramProvider provider = getBuilder().getProvider();

            return edges
                .stream()
                .map(modulesUmlEdge -> {
                    final Module from = modulesUmlEdge.getSource().getIdentifyingElement().getModule();
                    final ModuleDepDiagramItem fromItem = new ModuleDepDiagramItem(from, isCustomExtension(from));

                    final Module to = modulesUmlEdge.getTarget().getIdentifyingElement().getModule();
                    final ModuleDepDiagramItem toItem = new ModuleDepDiagramItem(to, isCustomExtension(to));

                    return new ModuleDepDiagramEdge(
                        new ModuleDepDiagramNode(fromItem, provider),
                        new ModuleDepDiagramNode(toItem, provider),
                        modulesUmlEdge.getRelationship()
                    );
                }).collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        @NotNull
        private List<ModuleDepDiagramNode> createAdaptedNodes(@NotNull final Collection<ModuleItem> items) {
            final DiagramProvider<ModuleDepDiagramItem> provider = (DiagramProvider<ModuleDepDiagramItem>) getBuilder().getProvider();

            return items.stream()
                        .map(moduleItem -> new ModuleDepDiagramNode(
                            new ModuleDepDiagramItem(moduleItem.getModule(), isCustomExtension(moduleItem.getModule())),
                            provider
                        )).collect(Collectors.toList());
        }
    }
}
