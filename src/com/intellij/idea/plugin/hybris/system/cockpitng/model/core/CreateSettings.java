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
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:CreateSettings interface.
 * <pre>
 * <h3>Type null:CreateSettings documentation</h3>
 * Template instances creation settings.
 * </pre>
 */
public interface CreateSettings extends DomElement {

    /**
     * Returns the value of the onInit child.
     * <pre>
     * <h3>Attribute null:onInit documentation</h3>
     * Determines if the template instance should be created automatically on template
     * 					initialization.
     * </pre>
     *
     * @return the value of the onInit child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("onInit")
    GenericAttributeValue<Boolean> getOnInit();


    /**
     * Returns the value of the reuseExisting child.
     * <pre>
     * <h3>Attribute null:reuseExisting documentation</h3>
     * Determines if new template instance should be created or rather reuse existing
     * 					instance.
     * </pre>
     *
     * @return the value of the reuseExisting child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("reuseExisting")
    GenericAttributeValue<Boolean> getReuseExisting();


    /**
     * Returns the value of the incoming-events child.
     * <pre>
     * <h3>Element null:incoming-events documentation</h3>
     * Lists incoming socket events for which new instance should be created
     * </pre>
     *
     * @return the value of the incoming-events child.
     */
    @NotNull
    @SubTag("incoming-events")
    SocketEvents getIncomingEvents();


    /**
     * Returns the value of the all-incoming-events child.
     * <pre>
     * <h3>Element null:all-incoming-events documentation</h3>
     * Determines if new instance should be created in case of any incoming socket event.
     * </pre>
     * <pre>
     * <h3>Type null:AllSocketEvents documentation</h3>
     * Marker tag. If present, all socket events defined for the given widget template will be
     * 				used in the
     * 				context when the tag is used.
     * </pre>
     *
     * @return the value of the all-incoming-events child.
     */
    @NotNull
    @SubTag("all-incoming-events")
    GenericDomValue<String> getAllIncomingEvents();


}
