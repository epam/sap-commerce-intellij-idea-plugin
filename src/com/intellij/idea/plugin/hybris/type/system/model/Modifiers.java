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

package com.intellij.idea.plugin.hybris.type.system.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * null:modifiersType interface.
 * <pre>
 * <h3>Type null:modifiersType documentation</h3>
 * Specifies further properties of an attribute which can be redeclared at other extensions.
 * </pre>
 */
public interface Modifiers extends DomElement {

    String DONT_OPTIMIZE = "dontOptimize";
    String ENCRYPTED = "encrypted";
    String READ = "read";
    String WRITE = "write";
    String SEARCH = "search";
    String OPTIONAL = "optional";
    String PRIVATE = "private";
    String INITIAL = "initial";
    String REMOVABLE = "removable";
    String PART_OF = "partof";
    String UNIQUE = "unique";

    /**
     * Returns the value of the read child.
     * <pre>
     * <h3>Attribute null:read documentation</h3>
     * Defines if this attribute is readable (that is, if a getter method will be generated). Default is 'true'.	The visibility of the getter depends on the value of the private attribute.
     * </pre>
     *
     * @return the value of the read child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(READ)
    GenericAttributeValue<Boolean> getRead();


    /**
     * Returns the value of the write child.
     * <pre>
     * <h3>Attribute null:write documentation</h3>
     * Defines if this attribute is writable (that is, if a setter method will be generated). Default is 'true'.	The visibility of the setter depends on the value of the private attribute.
     * </pre>
     *
     * @return the value of the write child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(WRITE)
    GenericAttributeValue<Boolean> getWrite();


    /**
     * Returns the value of the search child.
     * <pre>
     * <h3>Attribute null:search documentation</h3>
     * Defines if this attribute is searchable by a FlexibleSearch. Default is 'true'. Attributes with persistence type set to 'jalo' can not be searchable.
     * </pre>
     *
     * @return the value of the search child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(SEARCH)
    GenericAttributeValue<Boolean> getSearch();


    /**
     * Returns the value of the optional child.
     * <pre>
     * <h3>Attribute null:optional documentation</h3>
     * Defines if this attribute is mandatory or optional. Default is 'true' for optional. Set to 'false' for mandatory.
     * </pre>
     *
     * @return the value of the optional child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(OPTIONAL)
    GenericAttributeValue<Boolean> getOptional();


    /**
     * Returns the value of the private child.
     * <pre>
     * <h3>Attribute null:private documentation</h3>
     * Defines the Java visibility of the generated getter and setter methods for this attribute. If 'true', the visibility modifier of generated methods is set to 'protected'; if 'false', the visibility modifier is 'public'. Default is 'false' for 'public' generated methods. Also, you will have no generated methods in the ServiceLayer if 'true'.
     * </pre>
     *
     * @return the value of the private child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PRIVATE)
    GenericAttributeValue<Boolean> getPrivate();


    /**
     * Returns the value of the initial child.
     * <pre>
     * <h3>Attribute null:initial documentation</h3>
     * If 'true', the attribute will only be writable during the item creation. Setting this to 'true' is only useful in combination with write='false'.	Default is 'false'.
     * </pre>
     *
     * @return the value of the initial child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(INITIAL)
    GenericAttributeValue<Boolean> getInitial();


    /**
     * Returns the value of the removable child.
     * <pre>
     * <h3>Attribute null:removable documentation</h3>
     * Defines if this attribute is removable. Default is 'true'.
     * </pre>
     *
     * @return the value of the removable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(REMOVABLE)
    GenericAttributeValue<Boolean> getRemovable();


    /**
     * Returns the value of the partof child.
     * <pre>
     * <h3>Attribute null:partof documentation</h3>
     * Defines if the assigned attribute value only belongs to the current instance of this type. Default is 'false'.
     * </pre>
     *
     * @return the value of the partof child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PART_OF)
    GenericAttributeValue<Boolean> getPartOf();


    /**
     * Returns the value of the unique child.
     * <pre>
     * <h3>Attribute null:unique documentation</h3>
     * If 'true', the value of this attribute has to be unique within all instances of this type. If there are multiple attributes marked as unique, then their combined values must be unique. Will not be evaluated at jalo layer, if you want to manage the attribute directly using jalo layer you have to ensure uniqueness manually. Default is 'false'.
     * </pre>
     *
     * @return the value of the unique child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(UNIQUE)
    GenericAttributeValue<Boolean> getUnique();


    /**
     * Returns the value of the dontOptimize child.
     * <pre>
     * <h3>Attribute null:dontOptimize documentation</h3>
     * If 'true' the attribute value will be stored in the 'global' property table, otherwise a separate column (inside the table of the associated type)will be created for storing its values. Default is 'false'.
     * </pre>
     *
     * @return the value of the dontOptimize child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(DONT_OPTIMIZE)
    GenericAttributeValue<Boolean> getDoNotOptimize();


    /**
     * Returns the value of the encrypted child.
     * <pre>
     * <h3>Attribute null:encrypted documentation</h3>
     * If 'true', the attribute value will be stored in an encrypted way. Default is 'false'.
     * </pre>
     *
     * @return the value of the encrypted child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ENCRYPTED)
    GenericAttributeValue<Boolean> getEncrypted();


}
