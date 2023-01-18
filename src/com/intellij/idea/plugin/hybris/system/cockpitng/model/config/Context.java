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

// Generated on Wed Jan 18 00:41:57 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.config;

import com.intellij.idea.plugin.hybris.system.cockpitng.util.xml.ComponentConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config:context interface.
 */
public interface Context extends DomElement {

    /**
     * Returns the value of the merge-by child.
     *
     * @return the value of the merge-by child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-by")
    GenericAttributeValue<MergeAttrTypeKnown> getMergeBy();

    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the principal child.
     *
     * @return the value of the principal child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("principal")
    GenericAttributeValue<String> getPrincipal();


    /**
     * Returns the value of the component child.
     *
     * @return the value of the component child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("component")
    @Convert(ComponentConverter.class)
    GenericAttributeValue<String> getComponent();


    /**
     * Returns the value of the authority child.
     *
     * @return the value of the authority child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("authority")
    GenericAttributeValue<String> getAuthority();


    /**
     * Returns the list of context children.
     *
     * @return the list of context children.
     */
    @NotNull
    @SubTagList("context")
    java.util.List<Context> getContexts();

    /**
     * Adds new child to the list of context children.
     *
     * @return created child
     */
    @SubTagList("context")
    Context addContext();


}
