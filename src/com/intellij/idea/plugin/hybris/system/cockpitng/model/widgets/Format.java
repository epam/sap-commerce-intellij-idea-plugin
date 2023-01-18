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
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/perspectiveChooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/perspectiveChooser:format interface.
 */
public interface Format extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the title-key child.
     *
     * @return the value of the title-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title-key")
    GenericAttributeValue<String> getTitleKey();


    /**
     * Returns the value of the title child.
     *
     * @return the value of the title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title")
    GenericAttributeValue<String> getTitle();


    /**
     * Returns the value of the description-key child.
     *
     * @return the value of the description-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description-key")
    GenericAttributeValue<String> getDescriptionKey();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description")
    GenericAttributeValue<String> getDescription();


    /**
     * Returns the value of the icon-key child.
     *
     * @return the value of the icon-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("icon-key")
    GenericAttributeValue<String> getIconKey();


    /**
     * Returns the value of the icon child.
     *
     * @return the value of the icon child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("icon")
    GenericAttributeValue<String> getIcon();


}
