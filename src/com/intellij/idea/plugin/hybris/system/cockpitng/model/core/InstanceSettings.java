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
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:InstanceSettings interface.
 * <pre>
 * <h3>Type null:InstanceSettings documentation</h3>
 * Settings describing how template instances should work.
 * </pre>
 */
public interface InstanceSettings extends DomElement {

    /**
     * Returns the value of the socketEventRoutingMode child.
     * <pre>
     * <h3>Attribute null:socketEventRoutingMode documentation</h3>
     * Defines how socket events are forwarded to the template instances.
     * </pre>
     *
     * @return the value of the socketEventRoutingMode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("socketEventRoutingMode")
    GenericAttributeValue<SocketEventRoutingMode> getSocketEventRoutingMode();


    /**
     * Returns the value of the create child.
     * <pre>
     * <h3>Element null:create documentation</h3>
     * Defines when new template instance should be created.
     * </pre>
     *
     * @return the value of the create child.
     */
    @NotNull
    @SubTag("create")
    CreateSettings getCreate();


    /**
     * Returns the value of the close child.
     * <pre>
     * <h3>Element null:close documentation</h3>
     * Defines when new template instance should be closed.
     * </pre>
     *
     * @return the value of the close child.
     */
    @NotNull
    @SubTag("close")
    CloseSettings getClose();


    /**
     * Returns the value of the select child.
     * <pre>
     * <h3>Element null:select documentation</h3>
     * Defines when new template instance should be selected (shown).
     * </pre>
     *
     * @return the value of the select child.
     */
    @NotNull
    @SubTag("select")
    SelectSettings getSelect();


}
