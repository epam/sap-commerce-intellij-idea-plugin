/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.typeSystem.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:customPropertyType interface.
 * <pre>
 * <h3>Type null:customPropertyType documentation</h3>
 * Defines a custom property.
 * </pre>
 */
public interface CustomProperty extends DomElement {

    String NAME = "name";
    String VALUE = "value";

    /**
     * Returns the value of the name child.
     * <pre>
     * <h3>Attribute null:name documentation</h3>
     * The name of the custom property.
     * </pre>
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(NAME)
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the value child.
     * <pre>
     * <h3>Element null:value documentation</h3>
     * The value of the custom property.
     * </pre>
     *
     * @return the value of the value child.
     */
    @NotNull
    @SubTag(VALUE)
    @Required
    GenericDomValue<String> getValue();


}
