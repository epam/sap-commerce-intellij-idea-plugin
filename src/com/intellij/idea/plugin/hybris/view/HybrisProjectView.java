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

package com.intellij.idea.plugin.hybris.view;

import com.google.common.collect.Iterables;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode;
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleGroupNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeOptions;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created 10:14 PM 27 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class HybrisProjectView implements TreeStructureProvider, DumbAware {

    protected final Project project;
    protected final HybrisProjectSettingsComponent hybrisProjectSettingsComponent;
    protected final HybrisProjectSettings hybrisProjectSettings;
    protected final HybrisApplicationSettings hybrisApplicationSettings;
    private final String[] commerceGroupName;
    private final String[] platformGroupName;
    private final String[] ccv2GroupName;

    public HybrisProjectView(@NotNull final Project project) {
        Validate.notNull(project);

        this.project = project;
        this.hybrisProjectSettingsComponent = HybrisProjectSettingsComponent.getInstance(project);
        this.hybrisProjectSettings = hybrisProjectSettingsComponent.getState();
        this.hybrisApplicationSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        this.commerceGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupHybris());
        this.platformGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupPlatform());
        this.ccv2GroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupCCv2());
    }

    @Override
    @NotNull
    public Collection<AbstractTreeNode<?>> modify(
        @NotNull final AbstractTreeNode<?> parent,
        @NotNull final Collection<AbstractTreeNode<?>> children,
        final ViewSettings settings
    ) {
        Validate.notNull(parent);
        Validate.notNull(children);

        if (this.isNotHybrisProject()) {
            return children;
        }

        if (parent instanceof JunkProjectViewNode) {
            return this.isCompactEmptyMiddleFoldersEnabled(settings)
                ? this.compactEmptyMiddlePackages(parent, children)
                : children;
        }

        if (parent instanceof ProjectViewModuleGroupNode) {
            modifyIcons((ProjectViewModuleGroupNode) parent, children);
        }

        if (parent instanceof ExternalLibrariesNode) {
            return this.modifyExternalLibrariesNodes(children);
        }

        final Collection<AbstractTreeNode<?>> childrenWithProcessedJunkFiles = this.processJunkFiles(children, settings);

        return this.isCompactEmptyMiddleFoldersEnabled(settings)
            ? this.compactEmptyMiddlePackages(parent, childrenWithProcessedJunkFiles)
            : childrenWithProcessedJunkFiles;
    }

    private void modifyIcons(
        final ProjectViewModuleGroupNode parent,
        final Collection<AbstractTreeNode<?>> children
    ) {
        if (parent.getValue() != null) {
            final ModuleGroup moduleGroup = parent.getValue();
            if (Arrays.equals(moduleGroup.getGroupPath(), commerceGroupName) ||
                Arrays.equals(moduleGroup.getGroupPath(), platformGroupName)) {
                parent.getPresentation().setIcon(HybrisIcons.HYBRIS);
            }
            if (Arrays.equals(moduleGroup.getGroupPath(), ccv2GroupName)) {
                parent.getPresentation().setIcon(HybrisIcons.MODULE_CCV2_GROUP);
            }
        }
        final var settingsComponent = HybrisProjectSettingsComponent.getInstance(project);
        children.stream()
                .filter(PsiDirectoryNode.class::isInstance)
                .filter(child -> child.getParent() == null)
                .map(PsiDirectoryNode.class::cast)
                .forEach(child -> {
                    final VirtualFile vf = child.getVirtualFile();
                    if (vf == null) {
                        return;
                    }
                    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vf);
                    if (module == null) {
                        return;
                    }
                    final HybrisModuleDescriptorType type = settingsComponent.getModuleSettings(module).getDescriptorType();
                    if (type == null) {
                        return;
                    }
                    switch (type) {
                        case PLATFORM:
                        case EXT:
                        case OOTB:
                            parent.getPresentation().setIcon(HybrisIcons.HYBRIS);
                            final AbstractTreeNode superParent = parent.getParent();
                            if (superParent != null) {
                                superParent.getPresentation().setIcon(HybrisIcons.HYBRIS);
                            }
                            break;
                        case CCV2:
                            child.getPresentation().setIcon(HybrisIcons.MODULE_CCV2);
                            break;
                    }
                });
    }

    protected boolean isCompactEmptyMiddleFoldersEnabled(@Nullable final NodeOptions settings) {
        return this.hybrisApplicationSettings.getHideEmptyMiddleFolders()
               && (null != settings)
               && settings.isHideEmptyMiddlePackages();
    }

    @NotNull
    protected Collection<AbstractTreeNode<?>> modifyExternalLibrariesNodes(
        @NotNull final Collection<AbstractTreeNode<?>> children
    ) {
        Validate.notNull(children);

        final Collection<AbstractTreeNode<?>> treeNodes = new ArrayList<>();

        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiDirectoryNode) {
                final VirtualFile virtualFile = ((PsiDirectoryNode) child).getVirtualFile();

                if (null == virtualFile) {
                    continue;
                }

                if (!HybrisConstants.CLASSES_DIRECTORY.equalsIgnoreCase(virtualFile.getName())) {
                    treeNodes.add(child);
                }
            } else {
                treeNodes.add(child);
            }
        }

        return treeNodes;
    }

    @NotNull
    protected Collection<AbstractTreeNode<?>> processJunkFiles(
        @NotNull final Collection<AbstractTreeNode<?>> children,
        @Nullable final ViewSettings settings
    ) {
        Validate.notNull(children);

        final List<String> junkFileNames = this.getJunkFileNames();

        if (junkFileNames == null || junkFileNames.isEmpty()) {
            return children;
        }

        final List<AbstractTreeNode<?>> junkTreeNodes = new ArrayList<>();
        final Collection<AbstractTreeNode<?>> treeNodes = new ArrayList<>();

        for (AbstractTreeNode child : children) {
            if (child instanceof BasePsiNode) {
                final VirtualFile virtualFile = ((BasePsiNode) child).getVirtualFile();

                if (null == virtualFile) {
                    continue;
                }

                if (this.isJunk(virtualFile, junkFileNames)) {
                    junkTreeNodes.add(child);
                } else {
                    treeNodes.add(child);
                }

            } else {
                treeNodes.add(child);
            }
        }

        if (!junkTreeNodes.isEmpty()) {
            treeNodes.add(new JunkProjectViewNode(this.project, junkTreeNodes, settings));
        }

        return treeNodes;
    }

    @NotNull
    protected Collection<AbstractTreeNode<?>> compactEmptyMiddlePackages(
        @NotNull final AbstractTreeNode<?> parent,
        @NotNull final Collection<AbstractTreeNode<?>> children
    ) {
        Validate.notNull(parent);
        Validate.notNull(children);

        if (CollectionUtils.isEmpty(children)) {
            return children;
        }

        if (parent instanceof PsiDirectoryNode) {
            final PsiDirectoryNode parentPsiDirectoryNode = (PsiDirectoryNode) parent;
            final VirtualFile parentVirtualFile = parentPsiDirectoryNode.getVirtualFile();

            if (null != parentVirtualFile && this.isFileInRoots(parentVirtualFile)) {
                return children;
            }
        }

        final Collection<AbstractTreeNode<?>> compactedChildren = new ArrayList<>();

        for (AbstractTreeNode<?> child : children) {

            final AbstractTreeNode<?> compactedChild = this.recursivelyCompactEmptyMiddlePackages(
                child, child.getChildren()
            );

            compactedChildren.add(compactedChild);
        }

        return compactedChildren;
    }

    @Nullable
    protected AbstractTreeNode<?> recursivelyCompactEmptyMiddlePackages(
        @NotNull final AbstractTreeNode<?> parent,
        @Nullable final Collection<? extends AbstractTreeNode<?>> children
    ) {
        Validate.notNull(parent);

        if (CollectionUtils.isEmpty(children)) {
            return parent;
        }

        if (parent instanceof JunkProjectViewNode) {
            return parent;
        }

        if ((parent instanceof PsiDirectoryNode) && (children.size() == 1)) {
            final AbstractTreeNode<?> onlyChild = Iterables.getOnlyElement(children);

            if (onlyChild instanceof PsiDirectoryNode) {
                final PsiDirectoryNode parentPsiDirectoryNode = (PsiDirectoryNode) parent;
                final VirtualFile parentVirtualFile = parentPsiDirectoryNode.getVirtualFile();

                if (null == parentVirtualFile) {
                    return parent;
                }

                if (this.isFileInRoots(parentVirtualFile) || this.isSrcOrClassesDirectory(parentVirtualFile)) {
                    return parent;
                }

                final PsiDirectoryNode onlyChildPsiDirectoryNode = (PsiDirectoryNode) onlyChild;
                final VirtualFile onlyChildVirtualFile = onlyChildPsiDirectoryNode.getVirtualFile();

                if (null == onlyChildVirtualFile) {
                    return parent;
                }

                this.appendParentNameToOnlyChildName(
                    parentPsiDirectoryNode, parentVirtualFile, onlyChildPsiDirectoryNode, onlyChildVirtualFile
                );

                return this.recursivelyCompactEmptyMiddlePackages(onlyChild, onlyChild.getChildren());
            }
        }

        return parent;
    }

    private void appendParentNameToOnlyChildName(
        @NotNull final PsiDirectoryNode parentPsiDirectoryNode,
        @NotNull final VirtualFile parentVirtualFile,
        @NotNull final PsiDirectoryNode onlyChildPsiDirectoryNode,
        @NotNull final VirtualFile onlyChildVirtualFile
    ) {
        Validate.notNull(parentPsiDirectoryNode);
        Validate.notNull(parentVirtualFile);
        Validate.notNull(onlyChildPsiDirectoryNode);
        Validate.notNull(onlyChildVirtualFile);

        if (CollectionUtils.isEmpty(parentPsiDirectoryNode.getPresentation().getColoredText())) {
            onlyChildPsiDirectoryNode.getPresentation().addText(new ColoredFragment(
                parentVirtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES
            ));
        } else {
            for (ColoredFragment coloredFragment : parentPsiDirectoryNode.getPresentation().getColoredText()) {
                onlyChildPsiDirectoryNode.getPresentation().addText(coloredFragment);
            }
        }

        onlyChildPsiDirectoryNode.getPresentation().addText(new ColoredFragment(
            File.separator, SimpleTextAttributes.REGULAR_ATTRIBUTES
        ));

        onlyChildPsiDirectoryNode.getPresentation().addText(new ColoredFragment(
            onlyChildVirtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES
        ));
    }

    private boolean isFileInRoots(@NotNull final VirtualFile file) {
        Validate.notNull(file);

        final ProjectFileIndex index = ProjectRootManager.getInstance(this.project).getFileIndex();

        return index.isInSource(file) || index.isInLibraryClasses(file);
    }

    private boolean isSrcOrClassesDirectory(@NotNull final VirtualFile file) {
        Validate.notNull(file);

        return HybrisConstants.ADDON_SRC_DIRECTORY.equals(file.getName())
               || HybrisConstants.CLASSES_DIRECTORY.equals(file.getName())
               || HybrisConstants.TEST_CLASSES_DIRECTORY.equals(file.getName());
    }

    protected boolean isNotHybrisProject() {
        return null != hybrisProjectSettings && !hybrisProjectSettingsComponent.isHybrisProject();
    }

    protected boolean isJunk(@NotNull final VirtualFile virtualFile, @NotNull final List<String> junkFileNames) {
        Validate.notNull(virtualFile);
        Validate.notNull(junkFileNames);

        return junkFileNames.contains(virtualFile.getName()) || this.isIdeaModuleFile(virtualFile);
    }

    protected boolean isIdeaModuleFile(@NotNull final VirtualFile virtualFile) {
        Validate.notNull(virtualFile);

        return virtualFile.getName().endsWith(HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION);
    }

    @Nullable
    protected List<String> getJunkFileNames() {
        return HybrisApplicationSettingsComponent.getInstance().getState().getJunkDirectoryList();
    }

}
