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
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/advancedsearch:FieldListType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface FieldList extends DomElement {

    /**
     * Returns the value of the includeSubtypes child.
     *
     * @return the value of the includeSubtypes child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("includeSubtypes")
    GenericAttributeValue<Boolean> getIncludeSubtypes();


    /**
     * Returns the value of the disable-subtypes-checkbox child.
     *
     * @return the value of the disable-subtypes-checkbox child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-subtypes-checkbox")
    GenericAttributeValue<Boolean> getDisableSubtypesCheckbox();


    /**
     * Returns the value of the disable-attributes-comparator child.
     *
     * @return the value of the disable-attributes-comparator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-attributes-comparator")
    GenericAttributeValue<Boolean> getDisableAttributesComparator();


    /**
     * Returns the list of field children.
     *
     * @return the list of field children.
     */
    @NotNull
    @SubTagList("field")
    java.util.List<Field> getFields();

    /**
     * Adds new child to the list of field children.
     *
     * @return created child
     */
    @SubTagList("field")
    Field addField();


}
