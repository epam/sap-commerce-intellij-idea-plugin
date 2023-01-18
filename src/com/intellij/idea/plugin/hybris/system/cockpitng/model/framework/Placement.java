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

// Generated on Wed Jan 18 00:35:16 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package com.intellij.idea.plugin.hybris.system.cockpitng.model.framework;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:placement interface.
 */
public interface Placement extends DomElement {

    /**
     * Returns the value of the widgetId child.
     *
     * @return the value of the widgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetId")
    @Required
    GenericAttributeValue<String> getWidgetId();


    /**
     * Returns the value of the width child.
     *
     * @return the value of the width child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("width")
    GenericAttributeValue<Integer> getWidth();


    /**
     * Returns the value of the height child.
     *
     * @return the value of the height child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("height")
    GenericAttributeValue<Integer> getHeight();


    /**
     * Returns the value of the x child.
     *
     * @return the value of the x child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("x")
    GenericAttributeValue<Integer> getX();


    /**
     * Returns the value of the y child.
     *
     * @return the value of the y child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("y")
    GenericAttributeValue<Integer> getY();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


}
