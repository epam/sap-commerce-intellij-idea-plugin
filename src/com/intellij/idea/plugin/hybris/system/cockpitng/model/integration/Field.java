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
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/simplesearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model.integration;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.MergeMode;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Positioned;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/simplesearch:field interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Field extends DomElement, Positioned {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getNameAttr();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeModeAttr();


    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the operator child.
     *
     * @return the value of the operator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("operator")
    GenericAttributeValue<String> getOperator();


    /**
     * Returns the value of the selected child.
     *
     * @return the value of the selected child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("selected")
    GenericAttributeValue<Boolean> getSelected();


    /**
     * Returns the value of the editor child.
     *
     * @return the value of the editor child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("editor")
    GenericAttributeValue<String> getEditor();


    /**
     * Returns the value of the sortable child.
     *
     * @return the value of the sortable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sortable")
    GenericAttributeValue<Boolean> getSortable();


    /**
     * Returns the value of the disabled child.
     *
     * @return the value of the disabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disabled")
    GenericAttributeValue<Boolean> getDisabled();


    /**
     * Returns the value of the mandatory child.
     *
     * @return the value of the mandatory child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("mandatory")
    GenericAttributeValue<Boolean> getMandatory();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


    /**
     * Returns the list of editor-parameter children.
     *
     * @return the list of editor-parameter children.
     */
    @NotNull
    @SubTagList("editor-parameter")
    java.util.List<Parameter> getEditorParameters();

    /**
     * Adds new child to the list of editor-parameter children.
     *
     * @return created child
     */
    @SubTagList("editor-parameter")
    Parameter addEditorParameter();


}
