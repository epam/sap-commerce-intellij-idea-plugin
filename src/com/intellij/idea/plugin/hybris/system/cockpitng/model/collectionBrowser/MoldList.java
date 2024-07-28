/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

// Generated on Thu Jan 19 16:26:37 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/collectionbrowser

package com.intellij.idea.plugin.hybris.system.cockpitng.model.collectionBrowser;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.cockpitng.util.xml.CngMoldConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/collectionbrowser:mold-list interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface MoldList extends DomElement {

    String MOLD = "mold";

    /**
     * Returns the value of the default-mold child.
     *
     * @return the value of the default-mold child.
     */
    @NotNull
    @Attribute("default-mold")
    @Convert(CngMoldConverter.class)
    GenericAttributeValue<String> getDefaultMold();


    /**
     * Returns the list of mold children.
     *
     * @return the list of mold children.
     */
    @NotNull
    @SubTagList(MOLD)
    @Required
    java.util.List<Mold> getMolds();

    /**
     * Adds new child to the list of mold children.
     *
     * @return created child
     */
    @SubTagList(MOLD)
    Mold addMold();


}
