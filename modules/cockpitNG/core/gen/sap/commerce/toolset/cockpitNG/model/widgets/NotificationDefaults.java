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

// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package sap.commerce.toolset.cockpitNG.model.widgets;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationDefaults interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface NotificationDefaults extends DomElement {

    /**
     * Returns the value of the linksEnabled child.
     *
     * @return the value of the linksEnabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("linksEnabled")
    GenericAttributeValue<Boolean> getLinksEnabled();


    /**
     * Returns the value of the fallback child.
     *
     * @return the value of the fallback child.
     */
    @NotNull
    @SubTag("fallback")
    NotificationRenderingInfo getFallback();


    /**
     * Returns the value of the timeouts child.
     *
     * @return the value of the timeouts child.
     */
    @NotNull
    @SubTag("timeouts")
    NotificationTimeouts getTimeouts();


    /**
     * Returns the list of destinations children.
     *
     * @return the list of destinations children.
     */
    @NotNull
    @SubTagList("destinations")
    java.util.List<NotificationDestination> getDestinationses();

    /**
     * Adds new child to the list of destinations children.
     *
     * @return created child
     */
    @SubTagList("destinations")
    NotificationDestination addDestinations();


}
