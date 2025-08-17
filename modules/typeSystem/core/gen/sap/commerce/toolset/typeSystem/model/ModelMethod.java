/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import sap.commerce.toolset.xml.FalseAttributeValue;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:modelMethodType interface.
 * <pre>
 * <h3>Type null:modelMethodType documentation</h3>
 * Allows to configure alternative methods at generated model.
 * </pre>
 */
public interface ModelMethod extends DomElement {

    /**
     * Returns the value of the name child.
     * <pre>
     * <h3>Attribute null:name documentation</h3>
     * Name of the alternative getter method.
     * </pre>
     *
     * @return the value of the name child.
     */
    @NotNull
    @Attribute("name")
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the deprecated child.
     * <pre>
     * <h3>Attribute null:deprecated documentation</h3>
     * Will the method be marked deprecated?
     * Default is false.
     * </pre>
     *
     * @return the value of the deprecated child.
     */
    @NotNull
    @Attribute("deprecated")
    FalseAttributeValue getDeprecated();

    @NotNull
    @Attribute("deprecatedSince")
    GenericAttributeValue<String> getDeprecatedSince();


    /**
     * Returns the value of the default child.
     * <pre>
     * <h3>Attribute null:default documentation</h3>
     * Will this method be the default method and replace the original one instead of adding it additional?
     * Default is false.
     * </pre>
     *
     * @return the value of the default child.
     */
    @NotNull
    @Attribute("default")
    FalseAttributeValue getDefault();


}
