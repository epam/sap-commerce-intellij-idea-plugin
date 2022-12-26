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
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:attributeModelType interface.
 * <pre>
 * <h3>Type null:attributeModelType documentation</h3>
 * Allows to configure model generation for this attribute used at servicelayer.
 * </pre>
 */
public interface AttributeModel extends DomElement {

    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * Whether getter and setter methods for the model representation of the attribute will be generated. Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("generate")
    GenericAttributeValue<Boolean> getGenerate();


    /**
     * Returns the list of getter children.
     * <pre>
     * <h3>Element null:getter documentation</h3>
     * Allows to configure alternative getter methods at generated model.
     * </pre>
     *
     * @return the list of getter children.
     */
    @NotNull
    @SubTagList("getter")
    java.util.List<ModelMethod> getGetters();

    /**
     * Adds new child to the list of getter children.
     *
     * @return created child
     */
    @SubTagList("getter")
    ModelMethod addGetter();


    /**
     * Returns the list of setter children.
     * <pre>
     * <h3>Element null:setter documentation</h3>
     * Allows to configure alternative setter methods at generated model.
     * </pre>
     *
     * @return the list of setter children.
     */
    @NotNull
    @SubTagList("setter")
    java.util.List<ModelMethod> getSetters();

    /**
     * Adds new child to the list of setter children.
     *
     * @return created child
     */
    @SubTagList("setter")
    ModelMethod addSetter();


}
