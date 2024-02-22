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
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/valuechooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.MergeMode;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/valuechooser:value interface.
 */
public interface Value extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the expression child.
     *
     * @return the value of the expression child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("expression")
    @Required
    GenericAttributeValue<String> getExpression();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the bean-factory child.
     *
     * @return the value of the bean-factory child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("bean-factory")
    GenericAttributeValue<String> getBeanFactory();


    /**
     * Returns the value of the factory-method child.
     *
     * @return the value of the factory-method child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("factory-method")
    GenericAttributeValue<String> getFactoryMethod();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


}
