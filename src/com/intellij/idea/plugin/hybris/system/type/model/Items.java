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
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:itemsElemType interface.
 */
@Stubbed
@StubbedOccurrence
public interface Items extends DomElement {

    /**
     * Returns the value of the atomictypes child.
     * <pre>
     * <h3>Element null:atomictypes documentation</h3>
     * Defines the list of AtomicType's for your extension.
     * </pre>
     *
     * @return the value of the atomictypes child.
     */
    @NotNull
    @SubTag("atomictypes")
    AtomicTypes getAtomicTypes();


    /**
     * Returns the value of the collectiontypes child.
     * <pre>
     * <h3>Element null:collectiontypes documentation</h3>
     * Defines the list of CollectionType's for your extension.
     * </pre>
     *
     * @return the value of the collectiontypes child.
     */
    @NotNull
    @SubTag("collectiontypes")
    CollectionTypes getCollectionTypes();


    /**
     * Returns the value of the enumtypes child.
     * <pre>
     * <h3>Element null:enumtypes documentation</h3>
     * Defines the list of EnumerationType's for your extension.
     * </pre>
     *
     * @return the value of the enumtypes child.
     */
    @NotNull
    @SubTag("enumtypes")
    EnumTypes getEnumTypes();


    /**
     * Returns the value of the maptypes child.
     * <pre>
     * <h3>Element null:maptypes documentation</h3>
     * Defines the list of MapType's for your extension.
     * </pre>
     *
     * @return the value of the maptypes child.
     */
    @NotNull
    @SubTag("maptypes")
    MapTypes getMapTypes();


    /**
     * Returns the value of the relations child.
     * <pre>
     * <h3>Element null:relations documentation</h3>
     * Defines the list of RelationType's for your extension.
     * </pre>
     *
     * @return the value of the relations child.
     */
    @NotNull
    @SubTag("relations")
    Relations getRelations();


    /**
     * Returns the value of the itemtypes child.
     * <pre>
     * <h3>Element null:itemtypes documentation</h3>
     * Defines the list of ComposedType's for your extension.
     * </pre>
     *
     * @return the value of the itemtypes child.
     */
    @NotNull
    @SubTag("itemtypes")
    ItemTypes getItemTypes();


}
