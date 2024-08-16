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

/**
 * null:type-mappingElemType interface.
 */
@Stubbed
@StubbedOccurrence
public interface TypeMapping extends DomElement {

    String TYPE = "type";
    String PERSISTENCE_TYPE = "persistence-type";

    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    @Required
    @Stubbed
    @NameValue
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the persistence-type child.
     *
     * @return the value of the persistence-type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(PERSISTENCE_TYPE)
    @Required
    @Stubbed
    GenericAttributeValue<String> getPersistenceType();


}
