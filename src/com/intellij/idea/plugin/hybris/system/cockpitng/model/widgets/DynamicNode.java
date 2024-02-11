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
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/explorertree

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.util.xml.SpringBeanReferenceConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/explorertree:dynamic-node interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface DynamicNode extends DomElement, ExplorerNode {

    /**
     * Returns the value of the populator-bean-id child.
     *
     * @return the value of the populator-bean-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("populator-bean-id")
    @Referencing(SpringBeanReferenceConverter.class)
    GenericAttributeValue<String> getPopulatorBeanId();


    /**
     * Returns the value of the populator-class-name child.
     *
     * @return the value of the populator-class-name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("populator-class-name")
    GenericAttributeValue<String> getPopulatorClassName();


    /**
     * Returns the value of the indexing-depth child.
     *
     * @return the value of the indexing-depth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("indexing-depth")
    GenericAttributeValue<Integer> getIndexingDepth();


    /**
     * Returns the value of the hide-root-node child.
     *
     * @return the value of the hide-root-node child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("hide-root-node")
    GenericAttributeValue<Boolean> getHideRootNode();


    /**
     * Returns the list of dynamic-node-parameter children.
     *
     * @return the list of dynamic-node-parameter children.
     */
    @NotNull
    @SubTagList("dynamic-node-parameter")
    java.util.List<Parameter> getDynamicNodeParameters();

    /**
     * Adds new child to the list of dynamic-node-parameter children.
     *
     * @return created child
     */
    @SubTagList("dynamic-node-parameter")
    Parameter addDynamicNodeParameter();


}
