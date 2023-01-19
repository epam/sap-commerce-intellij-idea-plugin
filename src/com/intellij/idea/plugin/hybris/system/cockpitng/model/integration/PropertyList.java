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
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.integration;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:PropertyListType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface PropertyList extends DomElement {

    /**
     * Returns the value of the root child.
     *
     * @return the value of the root child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("root")
    GenericAttributeValue<String> getRoot();


    /**
     * Returns the value of the readonly child.
     *
     * @return the value of the readonly child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("readonly")
    GenericAttributeValue<Boolean> getReadonly();


    /**
     * Returns the value of the validate child.
     *
     * @return the value of the validate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("validate")
    GenericAttributeValue<Boolean> getValidate();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the include-non-declared-mandatory child.
     *
     * @return the value of the include-non-declared-mandatory child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("include-non-declared-mandatory")
    GenericAttributeValue<Boolean> getIncludeNonDeclaredMandatory();


    /**
     * Returns the value of the include-non-declared-unique child.
     *
     * @return the value of the include-non-declared-unique child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("include-non-declared-unique")
    GenericAttributeValue<Boolean> getIncludeNonDeclaredUnique();


    /**
     * Returns the value of the include-non-declared-writable-on-creation child.
     *
     * @return the value of the include-non-declared-writable-on-creation child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("include-non-declared-writable-on-creation")
    GenericAttributeValue<Boolean> getIncludeNonDeclaredWritableOnCreation();


    /**
     * Returns the value of the enable-non-declared-includes child.
     *
     * @return the value of the enable-non-declared-includes child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("enable-non-declared-includes")
    GenericAttributeValue<Boolean> getEnableNonDeclaredIncludes();


    /**
     * Returns the list of property children.
     *
     * @return the list of property children.
     */
    @NotNull
    @SubTagList("property")
    java.util.List<Property> getProperties();

    /**
     * Adds new child to the list of property children.
     *
     * @return created child
     */
    @SubTagList("property")
    Property addProperty();


}
