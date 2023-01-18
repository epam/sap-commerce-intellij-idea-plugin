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

// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/extendedsplitlayout

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/extendedsplitlayout:extended-split-layoutElemType interface.
 */
public interface ExtendedSplitLayout extends DomElement {

    /**
     * Returns the value of the defaultLayout child.
     *
     * @return the value of the defaultLayout child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("defaultLayout")
    GenericAttributeValue<String> getDefaultLayout();


    /**
     * Returns the list of layout-mapping children.
     *
     * @return the list of layout-mapping children.
     */
    @NotNull
    @SubTagList("layout-mapping")
    java.util.List<LayoutMapping> getLayoutMappings();

    /**
     * Adds new child to the list of layout-mapping children.
     *
     * @return created child
     */
    @SubTagList("layout-mapping")
    LayoutMapping addLayoutMapping();


}
