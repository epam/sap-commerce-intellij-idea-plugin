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
 * http://www.hybris.com/cockpitng/config/notifications:Notification interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Notification extends DomElement, NotificationRenderingInfo {

    String TYPE = "eventType";
    String LEVEL = "level";

    /**
     * Returns the value of the eventType child.
     *
     * @return the value of the eventType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    @Required
    GenericAttributeValue<String> getEventType();


    /**
     * Returns the value of the level child.
     *
     * @return the value of the level child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(LEVEL)
    @Required
    GenericAttributeValue<ImportanceLevel> getLevel();


    /**
     * Returns the value of the destination child.
     *
     * @return the value of the destination child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("destination")
    GenericAttributeValue<Destination> getDestination();


    /**
     * Returns the value of the referencesType child.
     *
     * @return the value of the referencesType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("referencesType")
    GenericAttributeValue<String> getReferencesType();

    /**
     * Returns the value of the message child.
     *
     * @return the value of the message child.
     */
    @NotNull
    @SubTag("message")
    GenericDomValue<String> getMessage();


    /**
     * Returns the value of the timeout child.
     *
     * @return the value of the timeout child.
     */
    @NotNull
    @SubTag("timeout")
    GenericDomValue<Integer> getTimeout();


    /**
     * Returns the value of the references child.
     *
     * @return the value of the references child.
     */
    @NotNull
    @SubTag("references")
    NotificationReferences getReferences();


}
