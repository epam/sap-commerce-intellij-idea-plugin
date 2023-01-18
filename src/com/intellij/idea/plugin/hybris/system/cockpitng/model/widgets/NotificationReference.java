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
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationReference interface.
 */
@Namespace(HybrisConstants.NAMESPACE_COCKPITNG_KEY)
public interface NotificationReference extends DomElement {

    /**
     * Returns the value of the index child.
     *
     * @return the value of the index child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("index")
    GenericAttributeValue<Integer> getIndex();


    /**
     * Returns the value of the placeholder child.
     *
     * @return the value of the placeholder child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("placeholder")
    @Required
    GenericAttributeValue<Integer> getPlaceholder();


    /**
     * Returns the value of the link child.
     *
     * @return the value of the link child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("link")
    GenericAttributeValue<String> getLink();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the message child.
     *
     * @return the value of the message child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("message")
    GenericAttributeValue<String> getMessage();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


    /**
     * Returns the list of context children.
     *
     * @return the list of context children.
     */
    @NotNull
    @SubTagList("context")
    java.util.List<NotificationLinkReferenceContext> getContexts();

    /**
     * Adds new child to the list of context children.
     *
     * @return created child
     */
    @SubTagList("context")
    NotificationLinkReferenceContext addContext();


}
