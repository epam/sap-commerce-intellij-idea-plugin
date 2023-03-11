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

import com.intellij.idea.plugin.hybris.system.type.file.CompositeConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:maptypeType interface.
 * <pre>
 * <h3>Type null:maptypeType documentation</h3>
 * Like the java collection framework, a type, which defines map objects. Attention: When used as type for an attribute, the attribute will not be searchable and the access performance is not effective. Consider to use a relation.
 * </pre>
 */
public interface MapType extends DomElement {

    String ARGUMENTTYPE = "argumenttype";
    String RETURNTYPE = "returntype";

    /**
     * Returns the value of the code child.
     * <pre>
     * <h3>Attribute null:code documentation</h3>
     * The unique code of the map.
     * </pre>
     *
     * @return the value of the code child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("code")
    @Required
    GenericAttributeValue<String> getCode();


    /**
     * Returns the value of the argumenttype child.
     * <pre>
     * <h3>Attribute null:argumenttype documentation</h3>
     * The type of the key attributes.
     * </pre>
     *
     * @return the value of the argumenttype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ARGUMENTTYPE)
    @Required
    @Convert(value = CompositeConverter.AnyClassifier.class, soft = true)
    GenericAttributeValue<String> getArgumentType();


    /**
     * Returns the value of the returntype child.
     * <pre>
     * <h3>Attribute null:returntype documentation</h3>
     * The type of the value attributes.
     * </pre>
     *
     * @return the value of the returntype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(RETURNTYPE)
    @Required
    @Convert(value = CompositeConverter.AnyClassifier.class, soft = true)
    GenericAttributeValue<String> getReturnType();


    /**
     * Returns the value of the autocreate child.
     * <pre>
     * <h3>Attribute null:autocreate documentation</h3>
     * If 'true', the item will be created during initialization. Default is 'true'.
     * </pre>
     *
     * @return the value of the autocreate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("autocreate")
    GenericAttributeValue<Boolean> getAutoCreate();


    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * Deprecated. Has no effect for map types. Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("generate")
    GenericAttributeValue<Boolean> getGenerate();


    /**
     * Returns the value of the redeclare child.
     * <pre>
     * <h3>Attribute null:redeclare documentation</h3>
     * Deprecated. Has no effect for map types. Default is 'false'.
     * </pre>
     *
     * @return the value of the redeclare child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("redeclare")
    GenericAttributeValue<Boolean> getRedeclare();


}
