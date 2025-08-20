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

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package sap.commerce.toolset.cockpitNG.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:Import interface.
 * <pre>
 * <h3>Type null:Import documentation</h3>
 * Specifies a resource for importing application configuration defined in another XML file.
 * 				Allows to
 * 				merge configuration that is distributed among many files.
 * </pre>
 */
public interface Import extends DomElement {

    /**
     * Returns the value of the resource child.
     *
     * @return the value of the resource child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("resource")
    @Required
    GenericAttributeValue<String> getResource();


    /**
     * Returns the value of the module-url child.
     *
     * @return the value of the module-url child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("module-url")
    GenericAttributeValue<String> getModuleUrl();


}
