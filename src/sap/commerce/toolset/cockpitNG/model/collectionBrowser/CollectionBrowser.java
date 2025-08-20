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

// Generated on Thu Jan 19 16:26:37 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/collectionbrowser

package sap.commerce.toolset.cockpitNG.model.collectionBrowser;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/config/collectionbrowser:collection-browserElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface CollectionBrowser extends DomElement {

    String AVAILABLE_MOLDS = "available-molds";
    String ENABLE_MULTI_SELECT = "enable-multi-select";

    /**
     * Returns the value of the enable-multi-select child.
     *
     * @return the value of the enable-multi-select child.
     */
    @NotNull
    @Attribute(ENABLE_MULTI_SELECT)
    GenericAttributeValue<Boolean> getEnableMultiSelect();


    /**
     * Returns the value of the available-molds child.
     *
     * @return the value of the available-molds child.
     */
    @NotNull
    @SubTag(AVAILABLE_MOLDS)
    MoldList getAvailableMolds();


}
