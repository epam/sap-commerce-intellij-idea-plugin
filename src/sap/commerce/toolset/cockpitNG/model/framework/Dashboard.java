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

// Generated on Wed Jan 18 00:35:16 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package sap.commerce.toolset.cockpitNG.model.framework;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:dashboardElemType interface.
 */
public interface Dashboard extends DomElement {

    /**
     * Returns the value of the defaultGridId child.
     *
     * @return the value of the defaultGridId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("defaultGridId")
    GenericAttributeValue<String> getDefaultGridId();


    /**
     * Returns the list of grid children.
     *
     * @return the list of grid children.
     */
    @NotNull
    @SubTagList("grid")
    @Required
    java.util.List<Grid> getGrids();

    /**
     * Adds new child to the list of grid children.
     *
     * @return created child
     */
    @SubTagList("grid")
    Grid addGrid();


}
