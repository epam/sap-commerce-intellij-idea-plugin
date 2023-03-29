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
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:indexKeyType interface.
 * <pre>
 * <h3>Type null:indexKeyType documentation</h3>
 * Configures a single index key.
 * </pre>
 */
public interface IndexKey extends DomElement {

    /**
     * Returns the value of the attribute child.
     * <pre>
     * <h3>Attribute null:attribute documentation</h3>
     * Type attribute to be indexed.
     * </pre>
     *
     * @return the value of the attribute child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("attribute")
    @Required
    GenericAttributeValue<String> getAttribute();


    /**
     * Returns the value of the lower child.
     * <pre>
     * <h3>Attribute null:lower documentation</h3>
     * Elements will be indexed case-insensitive. Default is 'false'.
     * </pre>
     *
     * @return the value of the lower child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("lower")
    FalseAttributeValue getLower();


}
