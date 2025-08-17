/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.typeSystem.model;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:persistenceType interface.
 * <pre>
 * <h3>Type null:persistenceType documentation</h3>
 * Defines how the values of the attribute will be stored. Possible values: 'cmp' (deprecated), 'jalo' (not persistent), and 'property' (persistent).
 * </pre>
 */
public interface Persistence extends DomElement {

    String TYPE = "type";
    String QUALIFIER = "qualifier";
    String ATTRIBUTE_HANDLER = "attributeHandler";
    String COLUMNTYPE = "columntype";

    /**
     * Returns the value of the type child.
     * <pre>
     * <h3>Attribute null:type documentation</h3>
     * Defines how the values of the attribute will be stored.
     * Possible values: 'cmp' (deprecated), 'jalo' (not persistent, deprecated), 'property' (persistent), 'dynamic' (not persisted).
     * </pre>
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    @Required
    GenericAttributeValue<PersistenceType> getType();


    /**
     * Returns the value of the qualifier child.
     * <pre>
     * <h3>Attribute null:qualifier documentation</h3>
     * Deprecated. Only usable in relation with 'cmp' and 'property'(compatibility reasons) persistence type. Default is empty.
     * </pre>
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(QUALIFIER)
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the attributeHandler child.
     * <pre>
     * <h3>Attribute null:attributeHandler documentation</h3>
     * Spring bean id that handles dynamic attributes implementation.
     * </pre>
     *
     * @return the value of the attributeHandler child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ATTRIBUTE_HANDLER)
    GenericAttributeValue<String> getAttributeHandler();


    /**
     * Returns the list of columntype children.
     * <pre>
     * <h3>Element null:columntype documentation</h3>
     * Configures a persistence definition for a specific database used at create statement.
     * </pre>
     *
     * @return the list of columntype children.
     */
    @NotNull
    @SubTagList(COLUMNTYPE)
    java.util.List<ColumnType> getColumnTypes();

    /**
     * Adds new child to the list of columntype children.
     *
     * @return created child
     */
    @SubTagList(COLUMNTYPE)
    ColumnType addColumnType();


}
