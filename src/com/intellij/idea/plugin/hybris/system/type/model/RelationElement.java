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

import com.intellij.idea.plugin.hybris.system.type.util.xml.converter.ItemTypeReferenceConverter;
import com.intellij.idea.plugin.hybris.system.type.util.xml.converter.RelationElementMetaTypeReferenceConverter;
import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.idea.plugin.hybris.util.xml.TrueAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * null:relationElementType interface.
 * <pre>
 * <h3>Type null:relationElementType documentation</h3>
 * Configures the generated attribute at one relation end.
 * </pre>
 */
public interface RelationElement extends DomElement {

    String TYPE = "type";
    String QUALIFIER = "qualifier";
    String META_TYPE = "metatype";
    String CARDINALITY = "cardinality";
    String NAVIGABLE = "navigable";
    String COLLECTION_TYPE = "collectiontype";
    String ORDERED = "ordered";

    /**
     * Returns the value of the type child.
     * <pre>
     * <h3>Attribute null:type documentation</h3>
     * Type of attribute which will be generated at type configured for opposite relation end.
     * </pre>
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(TYPE)
    @Required
    @Referencing(ItemTypeReferenceConverter.class)
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the qualifier child.
     * <pre>
     * <h3>Attribute null:qualifier documentation</h3>
     * Qualifier of attribute which will be generated at type configured for opposite relation end. If navigable is not set to false the qualifier is mandatory. Default is empty.
     * </pre>
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(QUALIFIER)
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the metatype child.
     * <pre>
     * <h3>Attribute null:metatype documentation</h3>
     * The (meta)type which describes the attributes type. Must be type extending RelationDescriptor. Default is 'RelationDescriptor'.
     * </pre>
     *
     * @return the value of the metatype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(META_TYPE)
    @Referencing(RelationElementMetaTypeReferenceConverter.class)
    GenericAttributeValue<String> getMetaType();


    /**
     * Returns the value of the cardinality child.
     * <pre>
     * <h3>Attribute null:cardinality documentation</h3>
     * The cardinality of this relation end. Choose 'one' for 'one' part of a 1:n relation or 'many' when part of a n:m relation. A 1:1 relation is not supported. Default is 'many'.
     * </pre>
     *
     * @return the value of the cardinality child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(CARDINALITY)
    RelationCardinalityValue getCardinality();


    /**
     * Returns the value of the navigable child.
     * <pre>
     * <h3>Attribute null:navigable documentation</h3>
     * Is the relation navigable from this side. Can only be disabled for one side of many to many relation. If disabled, no qualifier as well as modifiers can be defined. Default is 'true'.
     * </pre>
     *
     * @return the value of the navigable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(NAVIGABLE)
    TrueAttributeValue getNavigable();


    /**
     * Returns the value of the collectiontype child.
     * <pre>
     * <h3>Attribute null:collectiontype documentation</h3>
     * Configures the type of this collection if element has cardinality 'many'.
     * Related attribute getter / setter will use corresponding java collection interfaces.
     * Default is 'Collection'.
     * </pre>
     *
     * @return the value of the collectiontype child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(COLLECTION_TYPE)
    CollectionTypeValue getCollectionType();


    /**
     * Returns the value of the ordered child.
     * <pre>
     * <h3>Attribute null:ordered documentation</h3>
     * If 'true' an additional ordering attribute will be generated for maintaining ordering. Default is 'false'.
     * </pre>
     *
     * @return the value of the ordered child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute(ORDERED)
    FalseAttributeValue getOrdered();


    /**
     * Returns the value of the description child.
     * <pre>
     * <h3>Element null:description documentation</h3>
     * Documents this relation attribute. Will be cited at javadoc of generated getters/setters.
     * </pre>
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the modifiers child.
     * <pre>
     * <h3>Element null:modifiers documentation</h3>
     * Defines properties for the attribute.
     * </pre>
     *
     * @return the value of the modifiers child.
     */
    @NotNull
    @SubTag("modifiers")
    Modifiers getModifiers();


    /**
     * Returns the value of the model child.
     * <pre>
     * <h3>Element null:model documentation</h3>
     * Allows to configure model generation for this relation attribute used at servicelayer.
     * </pre>
     *
     * @return the value of the model child.
     */
    @NotNull
    @SubTag("model")
    AttributeModel getModel();


    /**
     * Returns the value of the custom-properties child.
     * <pre>
     * <h3>Element null:custom-properties documentation</h3>
     * Allows to configure custom properties for the relation attribute.
     * </pre>
     *
     * @return the value of the custom-properties child.
     */
    @NotNull
    @SubTag("custom-properties")
    CustomProperties getCustomProperties();


}
