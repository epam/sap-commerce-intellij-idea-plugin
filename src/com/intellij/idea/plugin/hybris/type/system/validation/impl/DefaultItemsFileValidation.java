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

package com.intellij.idea.plugin.hybris.type.system.validation.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.model.TypeGroup;
import com.intellij.idea.plugin.hybris.type.system.validation.ItemsFileValidation;
import com.intellij.idea.plugin.hybris.type.system.validation.TSRelationsValidation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ITEM_ROOT_CLASS;
import static com.intellij.idea.plugin.hybris.type.system.utils.TSUtils.getString;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class DefaultItemsFileValidation implements ItemsFileValidation {

    private static final Logger LOG = Logger.getInstance(DefaultItemsFileValidation.class);

    private static final ItemTypeClassValidation ITEM_TYPE_VALIDATION = new ItemTypeClassValidation();
    private static final EnumTypeClassValidation ENUM_TYPE_VALIDATION = new EnumTypeClassValidation();
    private static final TSRelationsValidation RELATIONS_VALIDATION = new DefaultTSRelationValidation();

    private Project project;

    public DefaultItemsFileValidation(@NotNull final Project project) {
        this.project = project;
    }

    @Override
    public boolean isFileOutOfDate(@NotNull final VirtualFile file) {
        if (file.getName().endsWith(HYBRIS_ITEMS_XML_FILE_ENDING)) {
            return this.isFileOutOfDateWithGeneratedClasses(file);
        }

        return false;
    }

    private boolean isFileOutOfDateWithGeneratedClasses(@NotNull final VirtualFile file) {
        try {
            final DomManager domManager = DomManager.getDomManager(this.project);

            final PsiManager psiManager = PsiManager.getInstance(this.project);
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile instanceof XmlFile) {
                final DomFileElement<Items> fileElement = domManager.getFileElement((XmlFile) psiFile, Items.class);
                if (null == fileElement) {
                    return true;
                }

                final Items itemsRootElement = fileElement.getRootElement();
                final Map<String, PsiClass> inheritedEnumClasses = this.findAllInheritClasses(
                    this.project, HybrisConstants.ENUM_ROOT_CLASS
                );

                final List<EnumType> enumTypes = itemsRootElement.getEnumTypes().getEnumTypes();
                if (ENUM_TYPE_VALIDATION.areGeneratedClassesOutOfDate(enumTypes, inheritedEnumClasses)) {
                    return true;
                }

                final Map<String, PsiClass> inheritedItemClasses = this.findAllInheritClasses(
                    this.project, ITEM_ROOT_CLASS
                );

                final List<ItemType> filteredItemTypes = this.getItemTypesExcludeRelations(itemsRootElement);
                if (ITEM_TYPE_VALIDATION.areGeneratedClassesOutOfDate(filteredItemTypes, inheritedItemClasses)) {
                    return true;
                }

                final List<TypeGroup> typeGroups = itemsRootElement.getItemTypes().getTypeGroups();
                for (TypeGroup typeGroup : typeGroups) {
                    final List<ItemType> groupedItemTypeList = typeGroup.getItemTypes();
                    if (ITEM_TYPE_VALIDATION.areGeneratedClassesOutOfDate(groupedItemTypeList, inheritedItemClasses)) {
                        return true;
                    }
                }

                final List<Relation> relations = itemsRootElement.getRelations().getRelations();
                if (RELATIONS_VALIDATION.validateRelations(relations, inheritedItemClasses)) {
                    return true;
                }
            }
        } catch (IndexNotReadyException ignore) {
            //do not validate Items.xml until index is not ready
        } catch (Exception e) {
            LOG.error(String.format("Items validation error. File: %s", file.getName()), e);
        }

        return false;
    }

    // Some people define relations as items and add attributes to them, we need to exclude those because
    // classes for them often are not being generated at all
    @NotNull
    private List<ItemType> getItemTypesExcludeRelations(@NotNull final Items itemsRootElement) {
        final Set<String> relationsMap = itemsRootElement.getRelations().getRelations().stream().map(
            relation -> getString(relation.getCode())
        ).collect(Collectors.toSet());

        return itemsRootElement.getItemTypes().getItemTypes().stream().filter(itemType -> !relationsMap.contains(
            getString(itemType.getCode())
        )).collect(Collectors.toList());
    }

    @NotNull
    private Map<String, PsiClass> findAllInheritClasses(
        @NotNull final Project project,
        @NotNull final String rootClass
    ) {
        final PsiClass itemRootClass = JavaPsiFacade.getInstance(project).findClass(
            rootClass, GlobalSearchScope.allScope(project)
        );

        if (null == itemRootClass) {
            return Collections.emptyMap();
        }

        final Collection<PsiClass> foundClasses = ClassInheritorsSearch.search(itemRootClass).findAll();
        final Map<String, PsiClass> result = new CaseInsensitiveMap<>();

        result.put(itemRootClass.getName(), itemRootClass);

        for (final PsiClass psiClass : foundClasses) {
            result.put(psiClass.getName(), psiClass);
        }

        return result;
    }
}
