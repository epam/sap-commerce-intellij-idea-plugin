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

// Generated on Wed Jan 18 00:44:24 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:labels interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Labels extends DomElement {

    /**
     * Returns the value of the beanId child.
     *
     * @return the value of the beanId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("beanId")
    GenericAttributeValue<String> getBeanId();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @SubTag("label")
    GenericDomValue<String> getLabel();


    /**
     * Returns the value of the shortLabel child.
     *
     * @return the value of the shortLabel child.
     */
    @NotNull
    @SubTag("shortLabel")
    GenericDomValue<String> getShortLabel();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the iconPath child.
     *
     * @return the value of the iconPath child.
     */
    @NotNull
    @SubTag("iconPath")
    GenericDomValue<String> getIconPath();


}
