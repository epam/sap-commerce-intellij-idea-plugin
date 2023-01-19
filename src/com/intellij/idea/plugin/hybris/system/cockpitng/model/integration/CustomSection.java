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

// Generated on Wed Jan 18 00:35:54 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model.integration;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:customSection interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface CustomSection extends DomElement, Section {

    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("class")
    GenericAttributeValue<String> getClazz();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    GenericAttributeValue<String> getSpringBean();

    /**
     * Returns the list of render-parameter children.
     *
     * @return the list of render-parameter children.
     */
    @NotNull
    @SubTagList("render-parameter")
    java.util.List<Parameter> getRenderParameters();

    /**
     * Adds new child to the list of render-parameter children.
     *
     * @return created child
     */
    @SubTagList("render-parameter")
    Parameter addRenderParameter();

}
