/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

// Generated on Sun Jun 05 01:21:13 EEST 2016
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.type.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:columntypeType interface.
 * <pre>
 * <h3>Type null:columntypeType documentation</h3>
 * Configures a persistence definition for a specific database.
 * </pre>
 */
public interface ColumnType extends DomElement {

    /**
     * Returns the value of the database child.
     * <pre>
     * <h3>Attribute null:database documentation</h3>
     * The database the given definition will be used for. One of 'oracle', 'mysql', 'sqlserver' or 'hsql'. Default is empty which configures fallback for non specified databases.
     * </pre>
     *
     * @return the value of the database child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("database")
    GenericAttributeValue<String> getDatabase();


    /**
     * Returns the value of the value child.
     * <pre>
     * <h3>Element null:value documentation</h3>
     * The attribute type used in the create statement of the database table, such as 'varchar2(4000)'.
     * </pre>
     *
     * @return the value of the value child.
     */
    @NotNull
    @SubTag("value")
    @Required
    GenericDomValue<String> getValue();


}
