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

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package sap.commerce.toolset.cockpitNG.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetMove interface.
 * <pre>
 * <h3>Type null:WidgetMove documentation</h3>
 * Specifies widget to be moved to as a child (by ID).
 * </pre>
 */
public interface WidgetMove extends DomElement {

    /**
     * Returns the value of the widgetId child.
     * <pre>
     * <h3>Attribute null:widgetId documentation</h3>
     * Current parent widget of extended widget
     * </pre>
     *
     * @return the value of the widgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetId")
    @Required
    GenericAttributeValue<String> getWidgetId();


    /**
     * Returns the value of the targetWidgetId child.
     * <pre>
     * <h3>Attribute null:targetWidgetId documentation</h3>
     * New parent widget of extended widget
     * </pre>
     *
     * @return the value of the targetWidgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("targetWidgetId")
    @Required
    GenericAttributeValue<String> getTargetWidgetId();


    /**
     * Returns the value of the targetSlotId child.
     * <pre>
     * <h3>Attribute null:targetSlotId documentation</h3>
     * Name of the slot in the new parent widget
     * </pre>
     *
     * @return the value of the targetSlotId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("targetSlotId")
    @Required
    GenericAttributeValue<String> getTargetSlotId();


    /**
     * Returns the value of the position child.
     * <pre>
     * <h3>Attribute null:position documentation</h3>
     * Position in the slot (useful when slot is defined as widgetchildren)
     * </pre>
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the failOnError child.
     *
     * @return the value of the failOnError child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("failOnError")
    GenericAttributeValue<Boolean> getFailOnError();


}
