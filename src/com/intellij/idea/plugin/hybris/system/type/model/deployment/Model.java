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
 * null:modelElemType interface.
 */
public interface Model extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description")
    GenericAttributeValue<String> getDescription();


    /**
     * Returns the list of database-schema children.
     *
     * @return the list of database-schema children.
     */
    @NotNull
    @SubTagList("database-schema")
    List<DatabaseSchema> getDatabaseSchemas();

    /**
     * Adds new child to the list of database-schema children.
     *
     * @return created child
     */
    @SubTagList("database-schema")
    DatabaseSchema addDatabaseSchema();


    /**
     * Returns the list of package children.
     *
     * @return the list of package children.
     */
    @NotNull
    @SubTagList("package")
    List<Package> getPackages();

    /**
     * Adds new child to the list of package children.
     *
     * @return created child
     */
    @SubTagList("package")
    Package addPackage();


}
