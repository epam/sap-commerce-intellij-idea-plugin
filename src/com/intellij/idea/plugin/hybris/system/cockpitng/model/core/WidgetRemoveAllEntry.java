/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.cockpitng.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetRemoveAllEntry interface.
 * <pre>
 * <h3>Type null:WidgetRemoveAllEntry documentation</h3>
 * Indicates that all children widgets should be removed as a result of the widget extension.
 * </pre>
 */
public interface WidgetRemoveAllEntry extends DomElement {

    /**
     * Returns the value of the contextId child.
     *
     * @return the value of the contextId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("contextId")
    GenericAttributeValue<String> getContextId();


    /**
     * Returns the value of the includeSettings child.
     * <pre>
     * <h3>Attribute null:includeSettings documentation</h3>
     * Indicates if settings of the target widget should be cleared.
     * </pre>
     *
     * @return the value of the includeSettings child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("includeSettings")
    GenericAttributeValue<Boolean> getIncludeSettings();


}
