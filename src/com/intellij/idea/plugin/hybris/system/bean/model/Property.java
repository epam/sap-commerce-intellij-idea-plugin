/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:property interface.
 */
public interface Property extends DomElement {

    String NAME = "name";
    String TYPE = "type";
    String EQUALS = "equals";
    String DEPRECATED = "deprecated";

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @Required
    @Attribute(NAME)
    //NOTE: We have to avoid @Convert since PsiField is in the read-only file and thus can't be renamed by platform
    //NOTE: Instead we are renaming the attribute value itself, see BeansRenamePsiElementProcessor
    //@Convert(soft = true, value = BeansPropertyNameConverter.class)
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @Required
    @Attribute(TYPE)
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the equals child.
     *
     * @return the value of the equals child.
     */
    @NotNull
    @Attribute(EQUALS)
    FalseAttributeValue getEquals();


    /**
     * Returns the value of the deprecated child.
     * <pre>
     * <h3>Attribute null:deprecated documentation</h3>
     * Marks property as deprecated. Allows defining a message.
     * </pre>
     *
     * @return the value of the deprecated child.
     */
    @NotNull
    @Attribute(DEPRECATED)
    FalseAttributeValue getDeprecated();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    Description getDescription();


    /**
     * Returns the list of annotations children.
     *
     * @return the list of annotations children.
     */
    @NotNull
    List<Annotations> getAnnotationses();

    @NotNull
    @SubTag("hints")
    Hints getHints();

    /**
     * Adds new child to the list of annotations children.
     *
     * @return created child
     */
    Annotations addAnnotations();


}
