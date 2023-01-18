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

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:section interface.
 */
public interface Section extends DomElement, AbstractSection {

    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeModeAttr();


    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getNameAttr();


    /**
     * Returns the value of the tooltipText child.
     *
     * @return the value of the tooltipText child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("tooltipText")
    GenericAttributeValue<String> getTooltipText();


    /**
     * Returns the value of the renderer child.
     *
     * @return the value of the renderer child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("renderer")
    GenericAttributeValue<String> getRenderer();


    /**
     * Returns the value of the initiallyOpened child.
     *
     * @return the value of the initiallyOpened child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("initiallyOpened")
    GenericAttributeValue<Boolean> getInitiallyOpened();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPositionAttr();


    /**
     * Returns the list of attribute children.
     *
     * @return the list of attribute children.
     */
    @NotNull
    @SubTagList("attribute")
    java.util.List<Attribute> getAttributes();

    /**
     * Adds new child to the list of attribute children.
     *
     * @return created child
     */
    @SubTagList("attribute")
    Attribute addAttribute();


    /**
     * Returns the list of custom-attribute children.
     *
     * @return the list of custom-attribute children.
     */
    @NotNull
    @SubTagList("custom-attribute")
    java.util.List<CustomAttribute> getCustomAttributes();

    /**
     * Adds new child to the list of custom-attribute children.
     *
     * @return created child
     */
    @SubTagList("custom-attribute")
    CustomAttribute addCustomAttribute();


    /**
     * Returns the list of actions children.
     *
     * @return the list of actions children.
     */
    @NotNull
    @SubTagList("actions")
    java.util.List<Actions> getActionses();

    /**
     * Adds new child to the list of actions children.
     *
     * @return created child
     */
    @SubTagList("actions")
    Actions addActions();


    /**
     * Returns the list of data-quality-group children.
     *
     * @return the list of data-quality-group children.
     */
    @NotNull
    @SubTagList("data-quality-group")
    java.util.List<DataQualityGroup> getDataQualityGroups();

    /**
     * Adds new child to the list of data-quality-group children.
     *
     * @return created child
     */
    @SubTagList("data-quality-group")
    DataQualityGroup addDataQualityGroup();


}
