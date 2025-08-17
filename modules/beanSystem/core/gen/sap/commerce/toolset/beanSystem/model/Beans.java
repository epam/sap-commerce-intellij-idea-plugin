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

// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package sap.commerce.toolset.beanSystem.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Stubbed;
import com.intellij.util.xml.StubbedOccurrence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:abstractPojos interface.
 */
@Stubbed
@StubbedOccurrence
public interface Beans extends DomElement {

    String BEAN = "bean";
    String ENUM = "enum";

    /**
     * Returns the list of bean children.
     *
     * @return the list of bean children.
     */
    @NotNull
    List<Bean> getBeans();

    /**
     * Adds new child to the list of bean children.
     *
     * @return created child
     */
    Bean addBean();


    /**
     * Returns the list of enum children.
     *
     * @return the list of enum children.
     */
    @NotNull
    List<Enum> getEnums();

    /**
     * Adds new child to the list of enum children.
     *
     * @return created child
     */
    Enum addEnum();


}
