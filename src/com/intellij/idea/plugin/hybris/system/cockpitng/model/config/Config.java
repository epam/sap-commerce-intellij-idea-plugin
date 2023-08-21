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

// Generated on Wed Jan 18 00:41:57 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.config;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Stubbed;
import com.intellij.util.xml.StubbedOccurrence;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config:config interface.
 */
@Stubbed
@StubbedOccurrence
public interface Config extends DomElement {

    String CONTEXT = "context";

    /**
     * Returns the value of the required-parameters child.
     *
     * @return the value of the required-parameters child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("required-parameters")
    GenericAttributeValue<String> getRequiredParameters();


    /**
     * Returns the list of requires children.
     *
     * @return the list of requires children.
     */
    @NotNull
    @SubTagList("requires")
    java.util.List<Requirement> getRequireses();

    /**
     * Adds new child to the list of requires children.
     *
     * @return created child
     */
    @SubTagList("requires")
    Requirement addRequires();


    /**
     * Returns the list of context children.
     *
     * @return the list of context children.
     */
    @NotNull
    @SubTagList(CONTEXT)
    java.util.List<Context> getContexts();

    /**
     * Adds new child to the list of context children.
     *
     * @return created child
     */
    @SubTagList(CONTEXT)
    Context addContext();


    /**
     * Returns the list of import children.
     *
     * @return the list of import children.
     */
    @NotNull
    @SubTagList("import")
    java.util.List<Import> getImports();

    /**
     * Adds new child to the list of import children.
     *
     * @return created child
     */
    @SubTagList("import")
    Import addImport();


}
