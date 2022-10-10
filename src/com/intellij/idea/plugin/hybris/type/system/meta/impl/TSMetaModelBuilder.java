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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.stubs.index.DomElementClassIndex;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaModelBuilder implements Processor<PsiFile> {

    private final Project myProject;
    private final DomManager myDomManager;

    private final Set<PsiFile> myFiles = new HashSet<>();

    public TSMetaModelBuilder(final Project project) {
        myProject = project;
        myDomManager = DomManager.getDomManager(project);
    }

    public Set<PsiFile> collectFiles() {
        myFiles.clear();

        StubIndex.getInstance().processElements(
            DomElementClassIndex.KEY,
            Items.class.getName(),
            myProject,
            ProjectScope.getAllScope(myProject),
            PsiFile.class,
            this
        );
        return Collections.unmodifiableSet(myFiles);
    }

    @NotNull
    public Set<PsiFile> getFiles() {
        return Collections.unmodifiableSet(myFiles);
    }

    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
    @Override
    public boolean process(final PsiFile psiFile) {
        final VirtualFile vFile = psiFile.getVirtualFile();

        if (vFile == null) {
            return true;
        }
        myFiles.add(psiFile);

        final DomFileElement<Items> rootWrapper = myDomManager.getFileElement((XmlFile) psiFile, Items.class);

        Optional.ofNullable(rootWrapper)
                .map(DomFileElement::getRootElement)
                .ifPresent(items -> {
                    items.getItemTypes().getItemTypes().forEach(this::processItemType);
                    items.getItemTypes().getTypeGroups().stream()
                         .flatMap(tg -> tg.getItemTypes().stream())
                         .forEach(this::processItemType);

                    items.getEnumTypes().getEnumTypes().forEach(this::processEnumType);
                    items.getAtomicTypes().getAtomicTypes().forEach(this::processAtomicType);
                    items.getCollectionTypes().getCollectionTypes().forEach(this::processCollectionType);
                    items.getRelations().getRelations().forEach(this::processRelationType);
                });

        //continue visiting
        return true;
    }

    private void processRelationType(final Relation relation) {
        TSMetaModelService.Companion.getInstance(myProject).findOrCreate(relation);
    }

    private void processAtomicType(final AtomicType atomicType) {
        TSMetaModelService.Companion.getInstance(myProject).findOrCreate(atomicType);
    }

    private void processEnumType(final @NotNull EnumType enumType) {
        final TSMetaEnum aEnum = TSMetaModelService.Companion.getInstance(myProject).findOrCreate(enumType);

        if (aEnum == null) return;

        enumType.getValues().forEach(aEnum::createValue);
    }

    private void processCollectionType(final @NotNull CollectionType collectionType) {
        TSMetaModelService.Companion.getInstance(myProject).findOrCreate(collectionType);
    }

    private void processItemType(final @NotNull ItemType itemType) {
        final TSMetaClass metaclass = TSMetaModelService.Companion.getInstance(myProject).findOrCreate(itemType);

        if (metaclass == null) return;

        itemType.getAttributes().getAttributes().stream()
                .map(domAttribute -> new TSMetaPropertyImpl(myProject, metaclass, domAttribute))
                .filter(property -> StringUtils.isNotBlank(property.getName()))
                .forEach(property -> metaclass.addProperty(property.getName().trim(), property));
    }
}
