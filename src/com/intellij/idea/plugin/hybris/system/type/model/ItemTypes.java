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

// Generated on Sun Jun 05 01:21:13 EEST 2016
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.type.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Stubbed;
import com.intellij.util.xml.StubbedOccurrence;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:itemtypesType interface.
 * <pre>
 * <h3>Type null:itemtypesType documentation</h3>
 * Defines a grouping of item types.
 * </pre>
 */
@Stubbed
@StubbedOccurrence
public interface ItemTypes extends DomElement {

    /**
     * Returns the list of itemtype children.
     * <pre>
     * <h3>Element null:itemtype documentation</h3>
     * Specifies a specific ComposedType.
     * </pre>
     *
     * @return the list of itemtype children.
     */
    @NotNull
    @SubTagList("itemtype")
    java.util.List<ItemType> getItemTypes();

    /**
     * Adds new child to the list of itemtype children.
     *
     * @return created child
     */
    @SubTagList("itemtype")
    ItemType addItemType();


    /**
     * Returns the list of typegroup children.
     * <pre>
     * <h3>Element null:typegroup documentation</h3>
     * Specifies a group of ComposedTypes to allow better structuring within the items.xml file.
     * </pre>
     *
     * @return the list of typegroup children.
     */
    @NotNull
    @SubTagList("typegroup")
    java.util.List<TypeGroup> getTypeGroups();

    /**
     * Adds new child to the list of typegroup children.
     *
     * @return created child
     */
    @SubTagList("typegroup")
    TypeGroup addTypeGroup();


}
