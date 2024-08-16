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

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:database-schemaElemType interface.
 */
@Stubbed
@StubbedOccurrence
public interface DatabaseSchema extends DomElement {

    String TYPE_MAPPING = "type-mapping";

    /**
     * Returns the value of the database child.
     *
     * @return the value of the database child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("database")
    @Required
    @Stubbed
    @NameValue
    GenericAttributeValue<String> getDatabase();


    /**
     * Returns the value of the primary-key child.
     *
     * @return the value of the primary-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("primary-key")
    GenericAttributeValue<String> getPrimaryKey();


    /**
     * Returns the value of the null child.
     *
     * @return the value of the null child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("null")
    GenericAttributeValue<String> getNull();


    /**
     * Returns the value of the not-null child.
     *
     * @return the value of the not-null child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("not-null")
    GenericAttributeValue<String> getNotNull();


    /**
     * Returns the list of type-mapping children.
     *
     * @return the list of type-mapping children.
     */
    @NotNull
    @SubTagList(TYPE_MAPPING)
    List<TypeMapping> getTypeMappings();

    /**
     * Adds new child to the list of type-mapping children.
     *
     * @return created child
     */
    @SubTagList(TYPE_MAPPING)
    TypeMapping addTypeMapping();


}
