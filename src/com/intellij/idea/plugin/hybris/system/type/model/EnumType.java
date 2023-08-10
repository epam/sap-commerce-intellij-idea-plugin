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

import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.idea.plugin.hybris.util.xml.TrueAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:enumtypeType interface.
 * <pre>
 * <h3>Type null:enumtypeType documentation</h3>
 * An EnumerationType defines fixed value types. (The typesystem provides item enumeration only)
 * </pre>
 */
public interface EnumType extends DomElement {

    String CODE = "code";
    String DYNAMIC = "dynamic";

    /**
     * Returns the value of the code child.
     * <pre>
     * <h3>Attribute null:code documentation</h3>
     * The unique code of this Enumeration.
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
     * Returns the value of the autocreate child.
     * <pre>
     * <h3>Attribute null:autocreate documentation</h3>
     * If 'true', the item will be created during initialization.
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
     * If 'false' no constants will be generated at constant class of extension as well as at corresponding servicelayer enum class. Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("generate")
    TrueAttributeValue getGenerate();


    /**
     * Returns the value of the jaloclass child.
     * <pre>
     * <h3>Attribute null:jaloclass documentation</h3>
     * Specifies the name of the associated jalo class. The specified class must extend de.hybris.platform.jalo.enumeration.EnumerationValue and will not be generated. By specifying a jalo class you can change the implementation to use for the values of this enumeration. By default EnumerationValue class is used.
     * </pre>
     *
     * @return the value of the jaloclass child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("jaloclass")
    GenericAttributeValue<String> getJaloClass();


    /**
     * Returns the value of the dynamic child.
     * <pre>
     * <h3>Attribute null:dynamic documentation</h3>
     * Whether it is possible to add new values by runtime.
     * Also results in different types of enums: 'true' results in 'classic' hybris enums, 'false' results in Java enums.
     * Default is false.
     * Both kinds of enums are API compatible, and switching between enum types is possible by running a system update.
     * </pre>
     *
     * @return the value of the dynamic child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(DYNAMIC)
    FalseAttributeValue getDynamic();


    /**
     * Returns the value of the description child.
     * <pre>
     * <h3>Element null:description documentation</h3>
     * Provides possibility to add meaningfull description phrase for a generated model class.
     * </pre>
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the model child.
     * <pre>
     * <h3>Element null:model documentation</h3>
     * Allows changing enum model settings.
     * </pre>
     *
     * @return the value of the model child.
     */
    @NotNull
    @SubTag("model")
    EnumModel getModel();


    /**
     * Returns the list of value children.
     * <pre>
     * <h3>Element null:value documentation</h3>
     * Configures one value of this Enumeration.
     * </pre>
     *
     * @return the list of value children.
     */
    @NotNull
    @SubTagList("value")
    java.util.List<EnumValue> getValues();

    /**
     * Adds new child to the list of value children.
     *
     * @return created child
     */
    @SubTagList("value")
    EnumValue addValue();


}
