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

import com.intellij.idea.plugin.hybris.system.cockpitng.util.xml.CngComponentConverter;
import com.intellij.idea.plugin.hybris.system.cockpitng.util.xml.CngMergeByConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config:context interface.
 */
public interface Context extends DomElement {

    String MERGE_BY = "merge-by";
    String TYPE = "type";
    String PRINCIPAL = "principal";
    String COMPONENT = "component";
    String AUTHORITY = "authority";
    String PARENT = "parent";
    String PARENT_AUTO = "auto";

    /**
     * Returns the value of the merge-by child.
     *
     * @return the value of the merge-by child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(MERGE_BY)
    // Actually, is can be anything beside MergeAttrTypeKnown
    @Convert(CngMergeByConverter.class)
    GenericAttributeValue<String> getMergeBy();

    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the principal child.
     *
     * @return the value of the principal child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PRINCIPAL)
    GenericAttributeValue<String> getPrincipal();


    /**
     * Returns the value of the component child.
     *
     * @return the value of the component child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(COMPONENT)
    @Convert(CngComponentConverter.class)
    GenericAttributeValue<String> getComponent();


    /**
     * Returns the value of the authority child.
     *
     * @return the value of the authority child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(AUTHORITY)
    GenericAttributeValue<String> getAuthority();


    /**
     * Returns the value of the parent child.
     *
     * @return the value of the parent child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PARENT)
    GenericAttributeValue<String> getParentAttribute();


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
