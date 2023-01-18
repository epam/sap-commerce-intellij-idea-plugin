/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetSetting interface.
 * <pre>
 * <h3>Type null:WidgetSetting documentation</h3>
 * Custom widget instance settings.
 * </pre>
 */
public interface WidgetSetting extends DomElement {

    /**
     * Returns the value of the simple content.
     *
     * @return the value of the simple content.
     */
    @NotNull
    @Required
    String getValue1();

    /**
     * Sets the value of the simple content.
     *
     * @param value1 the new value to set
     */
    void setValue1(@NotNull String value1);


    /**
     * Returns the value of the key child.
     * <pre>
     * <h3>Attribute null:key documentation</h3>
     * Setting key.
     * </pre>
     *
     * @return the value of the key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("key")
    @Required
    GenericAttributeValue<String> getKey();


    /**
     * Returns the value of the value child.
     * <pre>
     * <h3>Attribute null:value documentation</h3>
     * Setting value.
     * </pre>
     *
     * @return the value of the value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value")
    GenericAttributeValue<String> getValue2();


    /**
     * Returns the value of the type child.
     * <pre>
     * <h3>Attribute null:type documentation</h3>
     * Setting type (String, Boolean, Integer or Double).
     * </pre>
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<Setting> getType();


}
