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
 * null:modelConstructorType interface.
 * <pre>
 * <h3>Type null:modelConstructorType documentation</h3>
 * Allows to configure model constructor signatures.
 * </pre>
 */
public interface ModelConstructor extends DomElement {

    /**
     * Returns the value of the signature child.
     * <pre>
     * <h3>Attribute null:signature documentation</h3>
     * Add here, as comma separated list, the attribute qualifiers for the constructor signature in the model.
     * </pre>
     *
     * @return the value of the signature child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("signature")
    @Required
    GenericAttributeValue<String> getSignature();


}
