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

// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:abstractPojo interface.
 */
public interface AbstractPojo extends DomElement {

    String CLASS = "class";
    String DEPRECATED = "deprecated";
    String DEPRECATED_SINCE = "deprecatedSince";
    String TEMPLATE = "template";
    String DESCRIPTION = "description";

    /**
     * Returns the value of the template child.
     *
     * @return the value of the template child.
     */
    @NotNull
    @Attribute(TEMPLATE)
    GenericAttributeValue<String> getTemplate();


}
