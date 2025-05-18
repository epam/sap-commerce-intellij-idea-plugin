/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

// Generated on Sun Jun 05 01:21:13 EEST 2016
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.type.model;

import com.intellij.idea.plugin.hybris.system.type.util.xml.converter.ItemMetaTypeReferenceConverter;
import com.intellij.idea.plugin.hybris.system.type.util.xml.converter.ItemTypeReferenceConverter;
import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.idea.plugin.hybris.util.xml.TrueAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:itemtypeType interface.
 * <pre>
 * <h3>Type null:itemtypeType documentation</h3>
 * Specifies a specific ComposedType.
 * </pre>
 */
@Stubbed
@StubbedOccurrence
public interface ItemType extends DomElement {

    String CODE = "code";
    String EXTENDS = "extends";
    String JALO_CLASS = "jaloclass";
    String DEPLOYMENT = "deployment";
    String SINGLETON = "singleton";
    String JALO_ONLY = "jaloonly";
    String AUTO_CREATE = "autocreate";
    String GENERATE = "generate";
    String ABSTRACT = "abstract";
    String META_TYPE = "metatype";
    String DESCRIPTION = "description";
    String ATTRIBUTES = "attributes";
    String INDEXES = "indexes";

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
     * The unique code of this type.
     * </pre>
     *
     * @return the value of the code child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(CODE)
    @Required
    @Stubbed
    @NameValue
    GenericAttributeValue<String> getCode();


    /**
     * Returns the value of the extends child.
     * <pre>
     * <h3>Attribute null:extends documentation</h3>
     * Defines the class, which will be extended. Default is 'GenericItem'.
     * </pre>
     *
     * @return the value of the extends child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(EXTENDS)
    @Referencing(ItemTypeReferenceConverter.class)
    GenericAttributeValue<String> getExtends();


    /**
     * Returns the value of the jaloclass child.
     * <pre>
     * <h3>Attribute null:jaloclass documentation</h3>
     * Specifies the name of the associated jalo class. Default is [extension-root-package].jalo.[type-code] which will be generated if not existent.
     * </pre>
     *
     * @return the value of the jaloclass child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(JALO_CLASS)
    GenericAttributeValue<String> getJaloClass();


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
     * Returns the value of the singleton child.
     * <pre>
     * <h3>Attribute null:singleton documentation</h3>
     * If 'true', type gets marked as singleton which will be evaluated by some modules like hmc or impex, with that allowing only one instance per system.
     * Default is 'false'.
     * </pre>
     *
     * @return the value of the singleton child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(SINGLETON)
    FalseAttributeValue getSingleton();


    /**
     * Returns the value of the jaloonly child.
     * <pre>
     * <h3>Attribute null:jaloonly documentation</h3>
     * DEPRECATED. Use 'implements JaloOnlyItem' in your bean.
     * If 'true', the item will only exists in the jalo layer and isn't backed by an entity bean.
     * Default is 'false'.
     * </pre>
     *
     * @return the value of the jaloonly child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(JALO_ONLY)
    FalseAttributeValue getJaloOnly();


    /**
     * Returns the value of the autocreate child.
     * <pre>
     * <h3>Attribute null:autocreate documentation</h3>
     * If 'true', the item will be created during initialization. Default is 'true'.
     * </pre>
     *
     * @return the value of the autocreate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(AUTO_CREATE)
    TrueAttributeValue getAutoCreate();


    /**
     * Returns the value of the generate child.
     * <pre>
     * <h3>Attribute null:generate documentation</h3>
     * If 'true', the sourcecode for this item will be created. Default is 'true'.
     * </pre>
     *
     * @return the value of the generate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(GENERATE)
    TrueAttributeValue getGenerate();


    /**
     * Returns the value of the abstract child.
     * <pre>
     * <h3>Attribute null:abstract documentation</h3>
     * Marks type and jalo class as abstract. If 'true', the type can not be instantiated. Default is 'false'.
     * </pre>
     *
     * @return the value of the abstract child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ABSTRACT)
    FalseAttributeValue getAbstract();


    /**
     * Returns the value of the metatype child.
     * <pre>
     * <h3>Attribute null:metatype documentation</h3>
     * The (meta)type which describes the assigned type. Must be a type extensing ComposedType. Default is 'ComposedType'.
     * </pre>
     *
     * @return the value of the metatype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(META_TYPE)
    @Referencing(ItemMetaTypeReferenceConverter.class)
    GenericAttributeValue<String> getMetaType();


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
    Description getDescription();


    /**
     * Returns the value of the deployment child.
     * <pre>
     * <h3>Element null:deployment documentation</h3>
     * A deployment defines how a (generic) item or relation is mapped onto the database.
     * </pre>
     *
     * @return the value of the deployment child.
     */
    @NotNull
    @SubTag("deployment")
    Deployment getDeployment();


    /**
     * Returns the value of the custom-properties child.
     * <pre>
     * <h3>Element null:custom-properties documentation</h3>
     * Defines a list of custom properties for this type.
     * </pre>
     *
     * @return the value of the custom-properties child.
     */
    @NotNull
    @SubTag("custom-properties")
    CustomProperties getCustomProperties();


    /**
     * Returns the value of the attributes child.
     * <pre>
     * <h3>Element null:attributes documentation</h3>
     * Defines the list of item attributes.
     * </pre>
     *
     * @return the value of the attributes child.
     */
    @NotNull
    @SubTag(ATTRIBUTES)
    Attributes getAttributes();


    /**
     * Returns the value of the indexes child.
     * <pre>
     * <h3>Element null:indexes documentation</h3>
     * Defines the database indexes for this type.
     * </pre>
     *
     * @return the value of the indexes child.
     */
    @NotNull
    @SubTag(INDEXES)
    Indexes getIndexes();


    /**
     * Returns the value of the model child.
     * <pre>
     * <h3>Element null:model documentation</h3>
     * Allows to configure model generation for this item used at servicelayer.
     * </pre>
     *
     * @return the value of the model child.
     */
    @NotNull
    @SubTag("model")
    ItemModel getModel();


}
