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

// Generated on Wed Jan 18 00:35:54 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/advancedsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model.integration;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/advancedsearch:advanced-searchElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface AdvancedSearch extends DomElement {

    /**
     * Returns the value of the connection-operator child.
     *
     * @return the value of the connection-operator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("connection-operator")
    GenericAttributeValue<ConnectionOperator> getConnectionOperator();


    /**
     * Returns the value of the disable-auto-search child.
     *
     * @return the value of the disable-auto-search child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-auto-search")
    GenericAttributeValue<Boolean> getDisableAutoSearch();


    /**
     * Returns the value of the disable-simple-search child.
     *
     * @return the value of the disable-simple-search child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-simple-search")
    GenericAttributeValue<Boolean> getDisableSimpleSearch();


    /**
     * Returns the value of the field-list child.
     *
     * @return the value of the field-list child.
     */
    @NotNull
    @SubTag("field-list")
    @Required
    FieldList getFieldList();


    /**
     * Returns the value of the sort-field child.
     *
     * @return the value of the sort-field child.
     */
    @NotNull
    @SubTag("sort-field")
    SortField getSortField();


}
