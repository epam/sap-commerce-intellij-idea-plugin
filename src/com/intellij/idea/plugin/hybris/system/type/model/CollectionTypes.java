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
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:collectiontypesType interface.
 * <pre>
 * <h3>Type null:collectiontypesType documentation</h3>
 * Defines a list of collection types.
 * </pre>
 */
public interface CollectionTypes extends DomElement {

    String COLLECTIONTYPE = "collectiontype";

    /**
     * Returns the list of collectiontype children.
     * <pre>
     * <h3>Element null:collectiontype documentation</h3>
     * A CollectionType defines a collection of typed elements.
     * </pre>
     *
     * @return the list of collectiontype children.
     */
    @NotNull
    @SubTagList(COLLECTIONTYPE)
    java.util.List<CollectionType> getCollectionTypes();

    /**
     * Adds new child to the list of collectiontype children.
     *
     * @return created child
     */
    @SubTagList("collectiontype")
    CollectionType addCollectionType();


}
