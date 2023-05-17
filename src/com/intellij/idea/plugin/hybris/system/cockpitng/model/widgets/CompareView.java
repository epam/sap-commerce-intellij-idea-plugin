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
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/compareview

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:compare-viewElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface CompareView extends DomElement {

    /**
     * Returns the value of the editor-area-ctx child.
     *
     * @return the value of the editor-area-ctx child.
     */
    @NotNull
    @SubTag("editor-area-ctx")
    EditorAreaCtx getEditorAreaCtx();


    /**
     * Returns the value of the grid-view-ctx child.
     *
     * @return the value of the grid-view-ctx child.
     */
    @NotNull
    @SubTag("grid-view-ctx")
    GridViewCtx getGridViewCtx();


    /**
     * Returns the value of the header child.
     *
     * @return the value of the header child.
     */
    @NotNull
    @SubTag("header")
    Header getHeader();


    /**
     * Returns the list of section children.
     *
     * @return the list of section children.
     */
    @NotNull
    @SubTagList("section")
    java.util.List<Section> getSections();

    /**
     * Adds new child to the list of section children.
     *
     * @return created child
     */
    @SubTagList("section")
    Section addSection();


}
