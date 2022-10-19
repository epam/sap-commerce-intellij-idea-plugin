/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

// Generated on Sun Jun 05 01:21:13 EEST 2016
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.type.system.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:relationType interface.
 * <pre>
 * <h3>Type null:relationType documentation</h3>
 * A RelationType defines a n-m or 1-n relation between types.
 * </pre>
 */
public interface Relation extends DomElement {

    String DEPLOYMENT = "deployment";
    String CODE = "code";
    String LOCALIZED = "localized";
    String AUTOCREATE = "autocreate";
    String GENERATE = "generate";
    String DESCRIPTION = "description";

    /**
     * Returns the value of the simple content.
     *
     * @return the value of the simple content.
     */
    @NotNull
    @Required
    String getValue();

    /**
     * Sets the value of the simple content.
     *
     * @param value the new value to set
     */
    void setValue(@NotNull String value);


    /**
     * Returns the value of the code child.
     * <pre>
     * <h3>Attribute null:code documentation</h3>
     * The typecode.
     * </pre>
     *
     * @return the value of the code child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(CODE)
    @Required
    GenericAttributeValue<String> getCode();


    /**
     * Returns the value of the localized child.
     * <pre>
     * <h3>Attribute null:localized documentation</h3>
     * A localized n-m relation can have a link between two items for each language.
     * </pre>
     *
     * @return the value of the localized child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(LOCALIZED)
    @Required
    GenericAttributeValue<Boolean> getLocalized();


    /**
     * Returns the value of the deployment child.
     * <pre>
     * <h3>Attribute null:deployment documentation</h3>
     * Deprecated, please use separate deployment sub tag. All instances of this type will be stored in a separated database table. The value of this attribute represents a reference to the specified deployment in the corresponding 'advanced-deployment.xml'. Default is empty.
     * </pre>
     *
     * @return the value of the deployment child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(DEPLOYMENT)
    GenericAttributeValue<String> getDeploymentAttr();


    /**
     * Returns the value of the autocreate child.
     * <pre>
     * <h3>Attribute null:autocreate documentation</h3>
     * If 'true', the item will be created during initialization.
     * </pre>
     *
     * @return the value of the autocreate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(AUTOCREATE)
    GenericAttributeValue<Boolean> getAutoCreate();


    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * Deprecated. Will have no effect for relations.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(GENERATE)
    GenericAttributeValue<Boolean> getGenerate();


    /**
     * Returns the value of the description child.
     * <pre>
     * <h3>Element null:description documentation</h3>
     * Provides possibility to add meaningfull description phrase for a generated model class.
     * </pre>
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag(DESCRIPTION)
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the deployment child.
     * <pre>
     * <h3>Element null:deployment documentation</h3>
     * Configures deployment information for this relation (table name and typecode).
     * </pre>
     *
     * @return the value of the deployment child.
     */
    @NotNull
    @SubTag("deployment")
    Deployment getDeployment();


    /**
     * Returns the value of the sourceElement child.
     * <pre>
     * <h3>Element null:sourceElement documentation</h3>
     * Configures the generated attribute at source relation end
     * </pre>
     *
     * @return the value of the sourceElement child.
     */
    @NotNull
    @SubTag("sourceElement")
    @Required
    RelationElement getSourceElement();


    /**
     * Returns the value of the targetElement child.
     * <pre>
     * <h3>Element null:targetElement documentation</h3>
     * Configures the generated attribute at target relation end
     * </pre>
     *
     * @return the value of the targetElement child.
     */
    @NotNull
    @SubTag("targetElement")
    @Required
    RelationElement getTargetElement();


}
