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

import com.intellij.idea.plugin.hybris.util.xml.TrueAttributeValue;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:itemModelType interface.
 * <pre>
 * <h3>Type null:itemModelType documentation</h3>
 * Allows to configure model generation for this item used at servicelayer.
 * </pre>
 */
public interface ItemModel extends DomElement {

    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * Whether a model for the type and models for subtypes will be generated. Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("generate")
    TrueAttributeValue getGenerate();


    /**
     * Returns the list of constructor children.
     * <pre>
     * <h3>Element null:constructor documentation</h3>
     * Allows to configure model constructor signatures.
     * </pre>
     *
     * @return the list of constructor children.
     */
    @NotNull
    @SubTagList("constructor")
    java.util.List<ModelConstructor> getConstructors();

    /**
     * Adds new child to the list of constructor children.
     *
     * @return created child
     */
    @SubTagList("constructor")
    ModelConstructor addConstructor();


}
