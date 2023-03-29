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

import com.intellij.idea.plugin.hybris.system.type.util.xml.converter.CompositeConverter;
import com.intellij.idea.plugin.hybris.util.xml.TrueAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:collectiontypeType interface.
 * <pre>
 * <h3>Type null:collectiontypeType documentation</h3>
 * A CollectionType defines a collection of typed elements. Attention: If using a collection type for persistent attributes (not jalo) you can not search on that attribute and you are limited in size of collection. Consider to use a relation instead.
 * </pre>
 */
public interface CollectionType extends DomElement {

    String ELEMENTTYPE = "elementtype";
    String TYPE = "type";
    String CODE = "code";

    /**
     * Returns the value of the code child.
     * <pre>
     * <h3>Attribute null:code documentation</h3>
     * The code (that is, qualifier) of the CollectionType.
     * </pre>
     *
     * @return the value of the code child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(CODE)
    @Required
    @NameValue
    GenericAttributeValue<String> getCode();


    /**
     * Returns the value of the elementtype child.
     * <pre>
     * <h3>Attribute null:elementtype documentation</h3>
     * The type of elements of this CollectionType.
     * </pre>
     *
     * @return the value of the elementtype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ELEMENTTYPE)
    @Convert(CompositeConverter.TypeOrEnumOrAtomic.class)
    @Required
    GenericAttributeValue<String> getElementType();


    /**
     * Returns the value of the autocreate child.
     * <pre>
     * <h3>Attribute null:autocreate documentation</h3>
     * If 'true', the CollectionType will be created during initialization.
     * </pre>
     *
     * @return the value of the autocreate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("autocreate")
    TrueAttributeValue getAutoCreate();


    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * Deprecated. Has no effect for collection types.
     * Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("generate")
    TrueAttributeValue getGenerate();


    /**
     * Returns the value of the type child.
     * <pre>
     * <h3>Attribute null:type documentation</h3>
     * Configures the type of this collection: 'set', 'list', 'collection'.
     * The getter / setter methods will use corresponding Java collection interfaces.
     * Default is 'collection'.
     * </pre>
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    CollectionTypeValue getType();


}
