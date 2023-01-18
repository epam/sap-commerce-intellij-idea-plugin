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
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:editor-property interface.
 */
@Namespace(HybrisConstants.NAMESPACE_COCKPITNG_KEY)
public interface EditorProperty extends DomElement {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    @Required
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the readOnly child.
     *
     * @return the value of the readOnly child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("readOnly")
    GenericAttributeValue<Boolean> getReadOnly();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the editor child.
     *
     * @return the value of the editor child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("editor")
    GenericAttributeValue<String> getEditor();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @SubTag("label")
    GenericDomValue<String> getLabel();


    /**
     * Returns the list of parameter children.
     *
     * @return the list of parameter children.
     */
    @NotNull
    @SubTagList("parameter")
    java.util.List<Parameter> getParameters();

    /**
     * Adds new child to the list of parameter children.
     *
     * @return created child
     */
    @SubTagList("parameter")
    Parameter addParameter();


}
