/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

// Generated on Sun Aug 04 12:09:12 CEST 2024
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.type.model.deployment;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:attributeElemType interface.
 */
public interface Attribute extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the primkey-field child.
     *
     * @return the value of the primkey-field child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("primkey-field")
    GenericAttributeValue<String> getPrimkeyField();


    /**
     * Returns the list of attribute-mapping children.
     *
     * @return the list of attribute-mapping children.
     */
    @NotNull
    @SubTagList("attribute-mapping")
    List<AttributeMapping> getAttributeMappings();

    /**
     * Adds new child to the list of attribute-mapping children.
     *
     * @return created child
     */
    @SubTagList("attribute-mapping")
    AttributeMapping addAttributeMapping();


}
