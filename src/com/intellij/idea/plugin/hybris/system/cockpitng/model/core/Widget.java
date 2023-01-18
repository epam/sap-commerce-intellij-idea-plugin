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

import com.intellij.idea.plugin.hybris.system.cockpitng.util.xml.WidgetDefinitionIdConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:Widget interface.
 * <pre>
 * <h3>Type null:Widget documentation</h3>
 * Representation of widget instance. Widgets may be nested.
 * </pre>
 */
public interface Widget extends DomElement {

    /**
     * Returns the value of the id child.
     * <pre>
     * <h3>Attribute null:id documentation</h3>
     * Widget instance id.
     * </pre>
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the widgetDefinitionId child.
     * <pre>
     * <h3>Attribute null:widgetDefinitionId documentation</h3>
     * Widget definition id.
     * </pre>
     *
     * @return the value of the widgetDefinitionId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetDefinitionId")
    @Required
    @Convert(WidgetDefinitionIdConverter.class)
    GenericAttributeValue<String> getWidgetDefinitionId();


    /**
     * Returns the value of the slotId child.
     * <pre>
     * <h3>Attribute null:slotId documentation</h3>
     * Slot ID of the parent widget, where this widget instance resides.
     * </pre>
     *
     * @return the value of the slotId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("slotId")
    GenericAttributeValue<String> getSlotId();


    /**
     * Returns the value of the title child.
     * <pre>
     * <h3>Attribute null:title documentation</h3>
     * Widget title.
     * </pre>
     *
     * @return the value of the title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title")
    GenericAttributeValue<String> getTitle();


    /**
     * Returns the value of the template child.
     * <pre>
     * <h3>Attribute null:template documentation</h3>
     * Determines if the widget is a template widget. Template can be used to create new
     * 					widget instances
     * 					based on it.
     * </pre>
     *
     * @return the value of the template child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("template")
    GenericAttributeValue<Boolean> getTemplate();


    /**
     * Returns the value of the lastFocusedChildIndex child.
     *
     * @return the value of the lastFocusedChildIndex child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("lastFocusedChildIndex")
    GenericAttributeValue<Integer> getLastFocusedChildIndex();


    /**
     * Returns the value of the lastFocusedTemplateInstanceId child.
     *
     * @return the value of the lastFocusedTemplateInstanceId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("lastFocusedTemplateInstanceId")
    GenericAttributeValue<String> getLastFocusedTemplateInstanceId();


    /**
     * Returns the value of the access child.
     * <pre>
     * <h3>Attribute null:access documentation</h3>
     * Access restriction. Contains comma separated set of authorities with granted access to
     * 					the widget
     * 					instance.
     * </pre>
     *
     * @return the value of the access child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("access")
    GenericAttributeValue<String> getAccess();


    /**
     * Returns the list of widget children.
     * <pre>
     * <h3>Element null:widget documentation</h3>
     * Widgets embedded in the slots of THIS widget.
     * </pre>
     *
     * @return the list of widget children.
     */
    @NotNull
    @SubTagList("widget")
    java.util.List<Widget> getWidgets();

    /**
     * Adds new child to the list of widget children.
     *
     * @return created child
     */
    @SubTagList("widget")
    Widget addWidget();


    /**
     * Returns the value of the instance-settings child.
     *
     * @return the value of the instance-settings child.
     */
    @NotNull
    @SubTag("instance-settings")
    InstanceSettings getInstanceSettings();


    /**
     * Returns the list of setting children.
     * <pre>
     * <h3>Element null:setting documentation</h3>
     * Custom setting of the widget instance.
     * </pre>
     *
     * @return the list of setting children.
     */
    @NotNull
    @SubTagList("setting")
    java.util.List<WidgetSetting> getSettings();

    /**
     * Adds new child to the list of setting children.
     *
     * @return created child
     */
    @SubTagList("setting")
    WidgetSetting addSetting();


    /**
     * Returns the value of the virtual-sockets child.
     * <pre>
     * <h3>Element null:virtual-sockets documentation</h3>
     * A set of dynamic sockets. Even though virtual socket is not specified by widget
     * 				definition, application
     * 				designer can add them via widgets.xml or application orchestrator.
     * </pre>
     *
     * @return the value of the virtual-sockets child.
     */
    @NotNull
    @SubTag("virtual-sockets")
    VirtualSockets getVirtualSockets();


}
