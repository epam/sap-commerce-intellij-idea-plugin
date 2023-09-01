/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:indexType interface.
 * <pre>
 * <h3>Type null:indexType documentation</h3>
 * Configures a database index for enclosing type.
 * </pre>
 */
public interface Index extends DomElement {

    String REMOVE = "remove";
    String REPLACE = "replace";
    String UNIQUE = "unique";
    String NAME = "name";
    String KEY = "key";

    /**
     * Returns the value of the name child.
     * <pre>
     * <h3>Attribute null:name documentation</h3>
     * The name prefix of the index.
     * </pre>
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(NAME)
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the remove child.
     * <pre>
     * <h3>Attribute null:remove documentation</h3>
     * If 'true' this index will be ommitted while in initialization process even if there were precendent declarations.
     * This attribute has effect only if replace = true.
     * </pre>
     *
     * @return the value of the remove child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(REMOVE)
    FalseAttributeValue getRemove();


    /**
     * Returns the value of the replace child.
     * <pre>
     * <h3>Attribute null:replace documentation</h3>
     * If 'true' this index is a replacement/redeclaration for already existing index.
     * </pre>
     *
     * @return the value of the replace child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(REPLACE)
    FalseAttributeValue getReplace();


    /**
     * Returns the value of the unique child.
     * <pre>
     * <h3>Attribute null:unique documentation</h3>
     * If 'true', the value of this attribute has to be unique within all instances of this index.
     * Attributes with persistence type set to 'jalo' can not be unique.
     * Default is 'false'.
     * </pre>
     *
     * @return the value of the unique child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(UNIQUE)
    FalseAttributeValue getUnique();


    /**
     * Returns the value of the creationmode child.
     * <pre>
     * <h3>Attribute null:creationmode documentation</h3>
     * Determines index creation mode.
     * </pre>
     *
     * @return the value of the creationmode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("creationmode")
    GenericAttributeValue<CreationMode> getCreationMode();


    /**
     * Returns the list of key children.
     * <pre>
     * <h3>Element null:key documentation</h3>
     * Configures a single index key.
     * </pre>
     *
     * @return the list of key children.
     */
    @NotNull
    @SubTagList(KEY)
    java.util.List<IndexKey> getKeys();

    /**
     * Adds new child to the list of key children.
     *
     * @return created child
     */
    @SubTagList(KEY)
    IndexKey addKey();

    /**
     * Returns the list of include children.
     * <pre>
     * <h3>Element null:include documentation</h3>
     * Configures a single index include.
     * </pre>
     *
     * @return the list of include children.
     */
    @NotNull
    @SubTagList("include")
    java.util.List<IndexInclude> getIncludes();

    /**
     * Adds new child to the list of include children.
     *
     * @return created child
     */
    @SubTagList("include")
    IndexInclude addInclude();


}
