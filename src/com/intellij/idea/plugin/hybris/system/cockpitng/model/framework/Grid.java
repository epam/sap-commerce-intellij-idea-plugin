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

import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Mergeable;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:grid interface.
 */
public interface Grid extends DomElement, Mergeable {

    /**
     * Returns the value of the minScreenWidth child.
     *
     * @return the value of the minScreenWidth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("minScreenWidth")
    GenericAttributeValue<Integer> getMinScreenWidth();


    /**
     * Returns the value of the maxScreenWidth child.
     *
     * @return the value of the maxScreenWidth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("maxScreenWidth")
    GenericAttributeValue<Integer> getMaxScreenWidth();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the unassigned child.
     *
     * @return the value of the unassigned child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("unassigned")
    GenericAttributeValue<UnassignedBehavior> getUnassigned();


    /**
     * Returns the value of the rowHeight child.
     *
     * @return the value of the rowHeight child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("rowHeight")
    GenericAttributeValue<Integer> getRowHeight();


    /**
     * Returns the value of the fluid child.
     *
     * @return the value of the fluid child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("fluid")
    GenericAttributeValue<Boolean> getFluid();


    /**
     * Returns the list of placement children.
     *
     * @return the list of placement children.
     */
    @NotNull
    @SubTagList("placement")
    java.util.List<Placement> getPlacements();

    /**
     * Adds new child to the list of placement children.
     *
     * @return created child
     */
    @SubTagList("placement")
    Placement addPlacement();


}
