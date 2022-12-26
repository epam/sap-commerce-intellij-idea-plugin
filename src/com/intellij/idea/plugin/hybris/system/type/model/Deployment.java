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
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:deploymentType interface.
 * <pre>
 * <h3>Type null:deploymentType documentation</h3>
 * A deployment defines how a (generic) item or relation is mapped onto the database.
 * </pre>
 */
public interface Deployment extends DomElement {

    String TABLE = "table";
    String TYPE_CODE = "typecode";
    String PROPERTY_TABLE = "propertytable";

    /**
     * Returns the value of the table child.
     * <pre>
     * <h3>Attribute null:table documentation</h3>
     * The mapped database table. Must be globally unique.
     * </pre>
     *
     * @return the value of the table child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TABLE)
    @Required
    GenericAttributeValue<String> getTable();


    /**
     * Returns the value of the typecode child.
     * <pre>
     * <h3>Attribute null:typecode documentation</h3>
     * The mapped item type code. Must be globally unique
     * </pre>
     *
     * @return the value of the typecode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE_CODE)
    @Required
    GenericAttributeValue<String> getTypeCode();


    /**
     * Returns the value of the propertytable child.
     * <pre>
     * <h3>Attribute null:propertytable documentation</h3>
     * The mapped dump property database table to be used for this item. Default is 'props'.
     * </pre>
     *
     * @return the value of the propertytable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PROPERTY_TABLE)
    GenericAttributeValue<String> getPropertyTable();


}
