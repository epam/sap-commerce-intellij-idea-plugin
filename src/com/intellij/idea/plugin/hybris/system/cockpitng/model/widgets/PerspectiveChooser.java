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

// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/perspectiveChooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/perspectiveChooser:perspective-chooserElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface PerspectiveChooser extends DomElement {

    /**
     * Returns the value of the defaultPerspective child.
     *
     * @return the value of the defaultPerspective child.
     */
    @NotNull
    @SubTag("defaultPerspective")
    DefaultPerspective getDefaultPerspective();


    /**
     * Returns the list of authority children.
     *
     * @return the list of authority children.
     */
    @NotNull
    @SubTagList("authority")
    java.util.List<Authority> getAuthorities();

    /**
     * Adds new child to the list of authority children.
     *
     * @return created child
     */
    @SubTagList("authority")
    Authority addAuthority();


    /**
     * Returns the list of format children.
     *
     * @return the list of format children.
     */
    @NotNull
    @SubTagList("format")
    java.util.List<Format> getFormats();

    /**
     * Adds new child to the list of format children.
     *
     * @return created child
     */
    @SubTagList("format")
    Format addFormat();


}
