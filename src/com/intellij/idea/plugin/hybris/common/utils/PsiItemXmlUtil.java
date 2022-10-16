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

package com.intellij.idea.plugin.hybris.common.utils;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumTypes;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemTypes;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO Good solve will be a create index between items.xml and java classes
 *
 * @author Nosov Aleksandr
 */
public final class PsiItemXmlUtil {

    public static final String ITEM_TYPE_TAG_NAME = "itemtype";
    public static final String ENUM_TYPE_TAG_NAME = "enumtype";

    private PsiItemXmlUtil() {
    }

    public static List<XmlElement> findTags(final PsiClass psiClass, final String tagName) {
        final Project project = psiClass.getProject();
        final String psiClassName = psiClass.getName();

        if (psiClassName == null) {
            throw new IllegalStateException("class name must not be a null");
        }
        final String searchName = cleanSearchName(psiClassName);

        return FilenameIndex.getAllFilesByExt(project, "xml", GlobalSearchScope.allScope(project)).stream()
                            .filter(file -> file.getName().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING))
                            .map(file -> (XmlFile) PsiManager.getInstance(project).findFile(file))
                            .filter(Objects::nonNull)
                            .map(xmlFile -> {
                                final DomManager manager = DomManager.getDomManager(project);
                                return manager.getFileElement(xmlFile, DomElement.class);
                            })
                            .filter(Objects::nonNull)
                            .map(DomFileElement::getRootElement)
                            .filter(Items.class::isInstance)
                            .map(Items.class::cast)
                            .<List<XmlElement>>map(root -> {
                                switch (tagName) {
                                    case ITEM_TYPE_TAG_NAME:
                                        return findItems(searchName, root);
                                    case ENUM_TYPE_TAG_NAME:
                                        return findEnums(searchName, root);
                                    default:
                                        return Collections.emptyList();
                                }
                            })
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
    }

    private static List<XmlElement> findEnums(final String searchName, final Items root) {
        final EnumTypes sourceItems = root.getEnumTypes();
        final List<EnumType> enumTypes = sourceItems.getEnumTypes();
        return enumTypes.stream()
                        .filter(itemType -> searchName.equals(itemType.getCode().getStringValue()))
                        .map(DomElement::getXmlElement)
                        .collect(Collectors.toList());
    }

    private static List<XmlElement> findItems(final String searchName, final Items root) {
        final ItemTypes sourceItems = root.getItemTypes();
        final List<ItemType> itemTypes = sourceItems.getItemTypes();
        final Stream<ItemType> streamItemTypes = itemTypes.stream();
        final Stream<ItemType> streamItemGroups = sourceItems.getTypeGroups().stream()
                                                             .flatMap(typeGroup -> typeGroup.getItemTypes().stream())
                                                             .collect(Collectors.toList()).stream();
        return Stream.concat(streamItemTypes, streamItemGroups)
                     .filter(itemType -> searchName.equals(itemType.getCode().getStringValue()))
                     .map(DomElement::getXmlElement)
                     .collect(Collectors.toList());
    }

    private static String cleanSearchName(@NotNull final String searchName) {
        final int idx = searchName.lastIndexOf("Model");
        if (idx == -1) {
            return searchName;
        }
        return searchName.substring(0, idx);
    }
}
