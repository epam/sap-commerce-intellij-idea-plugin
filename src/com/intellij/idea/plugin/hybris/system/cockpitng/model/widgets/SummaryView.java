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
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:summary-viewElemType interface.
 */
public interface SummaryView extends DomElement, ImagePreview {

    /**
     * Returns the value of the display-title child.
     *
     * @return the value of the display-title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-title")
    GenericAttributeValue<Boolean> getDisplayTitle();


    /**
     * Returns the list of custom-section children.
     *
     * @return the list of custom-section children.
     */
    @NotNull
    @SubTagList("custom-section")
    java.util.List<CustomSection> getCustomSections();

    /**
     * Adds new child to the list of custom-section children.
     *
     * @return created child
     */
    @SubTagList("custom-section")
    CustomSection addCustomSection();


    /**
     * Returns the list of section children.
     *
     * @return the list of section children.
     */
    @NotNull
    @SubTagList("section")
    java.util.List<Section> getSections();

    /**
     * Adds new child to the list of section children.
     *
     * @return created child
     */
    @SubTagList("section")
    Section addSection();


}
