/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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

// Generated on Sun Aug 04 12:09:12 CEST 2024
// DTD/Schema  :    null

package sap.commerce.toolset.typeSystem.model.deployment;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:packageElemType interface.
 */
public interface Package extends DomElement {

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
     * Returns the list of package children.
     * <pre>
     * <h3>Element null:package documentation</h3>
     * ***********************************************************************
     *           *                           Session beans                             *
     *           ***********************************************************************
     *
     *           ***********************************************************************
     *           *                           Entity beans                              *
     *           ***********************************************************************
     * </pre>
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


    /**
     * Returns the value of the object child.
     *
     * @return the value of the object child.
     */
    @NotNull
    @SubTag("object")
    java.lang.Object getObject();


}
