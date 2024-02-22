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
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:action-group interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface ActionGroup extends DomElement, Positioned, Mergeable {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the show-group-header child.
     *
     * @return the value of the show-group-header child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-group-header")
    GenericAttributeValue<Boolean> getShowGroupHeader();


    /**
     * Returns the value of the show-separator child.
     *
     * @return the value of the show-separator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-separator")
    GenericAttributeValue<Boolean> getShowSeparator();


    /**
     * Returns the list of label children.
     *
     * @return the list of label children.
     */
    @NotNull
    @SubTagList("label")
    java.util.List<GenericDomValue<String>> getLabels();

    /**
     * Adds new child to the list of label children.
     *
     * @return created child
     */
    @SubTagList("label")
    GenericDomValue<String> addLabel();


    /**
     * Returns the list of action children.
     *
     * @return the list of action children.
     */
    @NotNull
    @SubTagList("action")
    java.util.List<Action> getActions();

    /**
     * Adds new child to the list of action children.
     *
     * @return created child
     */
    @SubTagList("action")
    Action addAction();


}
