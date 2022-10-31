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

package com.intellij.idea.plugin.hybris.type.system.structure.view;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.ColumnType;
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty;
import com.intellij.idea.plugin.hybris.type.system.model.Deployment;
import com.intellij.idea.plugin.hybris.type.system.model.Description;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue;
import com.intellij.idea.plugin.hybris.type.system.model.Index;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.MapType;
import com.intellij.idea.plugin.hybris.type.system.model.Modifiers;
import com.intellij.idea.plugin.hybris.type.system.model.Persistence;
import com.intellij.idea.plugin.hybris.type.system.model.PersistenceType;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.idea.plugin.hybris.type.system.model.TypeGroup;
import com.intellij.idea.plugin.hybris.type.system.model.Value;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.Function;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.structure.DomStructureTreeElement;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/2/17.
 */
public class TSStructureTreeElement extends DomStructureTreeElement {

    private final Function<DomElement, DomService.StructureViewMode> myDescriptor;
    private final DomElementNavigationProvider myNavigationProvider;

    public <T extends DomElement> TSStructureTreeElement(
        final T stableCopy,
        final Function<DomElement, DomService.StructureViewMode> myDescriptor,
        final DomElementNavigationProvider myNavigationProvider
    ) {
        super(stableCopy, myDescriptor, myNavigationProvider);
        this.myDescriptor = myDescriptor;
        this.myNavigationProvider = myNavigationProvider;
    }

    protected StructureViewTreeElement createChildElement(final DomElement element) {
        return new TSStructureTreeElement(element, myDescriptor, myNavigationProvider);
    }

    /**
     * Returns the name of the object to be presented in most renderers across the program.
     *
     * @return the object name.
     */
    @Nullable
    public String getPresentableText() {
        final DomElement dom = getElement();
        if (dom instanceof Attribute) {
            final GenericAttributeValue<String> qualifier = ((Attribute) dom).getQualifier();
            String value = resolveValue(qualifier);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof CollectionType) {
            final GenericAttributeValue<String> code = ((CollectionType) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof ColumnType) {
            final GenericAttributeValue<String> database = ((ColumnType) dom).getDatabase();
            String value = resolveValue(database);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof CustomProperty) {
            final GenericAttributeValue<String> name = ((CustomProperty) dom).getName();
            String value = resolveValue(name);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof Deployment) {
            final GenericAttributeValue<String> table = ((Deployment) dom).getTable();
            String value = resolveValue(table);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof EnumType) {
            final GenericAttributeValue<String> code = ((EnumType) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof EnumValue) {
            final GenericAttributeValue<String> code = ((EnumValue) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof Index) {
            final GenericAttributeValue<String> name = ((Index) dom).getName();
            String value = resolveValue(name);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof ItemType) {
            final GenericAttributeValue<String> code = ((ItemType) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof MapType) {
            final GenericAttributeValue<String> code = ((MapType) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof Persistence) {
            final GenericAttributeValue<PersistenceType> type = ((Persistence) dom).getType();
            if (type != null) {
                final String value = type.getStringValue();
                if (value != null) {
                    return value;
                }
            }
        }
        if (dom instanceof Relation) {
            final GenericAttributeValue<String> code = ((Relation) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof RelationElement) {
            final GenericAttributeValue<String> qualifier = ((RelationElement) dom).getQualifier();
            String value = resolveValue(qualifier);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof TypeGroup) {
            final GenericAttributeValue<String> name = ((TypeGroup) dom).getName();
            String value = resolveValue(name);
            if (value != null) {
                return value;
            }
        }
        if (dom instanceof Value) {
            final GenericAttributeValue<String> code = ((Value) dom).getCode();
            String value = resolveValue(code);
            if (value != null) {
                return value;
            }
        }
        return super.getPresentableText();
    }

    private String resolveValue(final GenericAttributeValue<String> attributeValue) {
        if (attributeValue != null) {
            return attributeValue.getStringValue();
        }
        return null;
    }

    /**
     * Returns the location of the object (for example, the package of a class). The location
     * string is used by some renderers and usually displayed as grayed text next to the item name.
     *
     * @return the location description, or null if none is applicable.
     */
    @Nullable
    public String getLocationString() {
        final DomElement dom = getElement();
        final String attrValue = localizeAttribute(dom);
        if (attrValue != null) {
            return attrValue;
        }
        final String itemTypeValue = localizeItemType(dom);
        if (itemTypeValue != null) {
            return itemTypeValue;
        }
        final String modifiersValue = localizeModifiers(dom);
        if (modifiersValue != null) {
            return modifiersValue;
        }
        final String descriptionValue = localizeDescription(dom);
        if (descriptionValue != null) {
            return descriptionValue;
        }
        final String xmlElementName = dom.getXmlElementName();
        if (xmlElementName == null) {
            return null;
        }

        if (xmlElementName.equalsIgnoreCase(getPresentableText())) {
            return null;
        }
        return xmlElementName;
    }

    private String localizeDescription(final DomElement dom) {
        if (dom instanceof Description) {
            final XmlElement xmlElement = dom.getXmlElement();
            if (xmlElement instanceof XmlTag) {
                final XmlTagValue value = ((XmlTag) xmlElement).getValue();
                if (value != null) {
                    return value.getTrimmedText();
                }
            }
        }
        return null;
    }

    private String localizeModifiers(final DomElement dom) {
        if (dom instanceof Modifiers) {
            final Modifiers modifiers = (Modifiers) dom;
            final List<String> modList = new ArrayList<>();
            resolveModifier(modifiers.getDoNotOptimize(), "doNotOptimize", modList);
            resolveModifier(modifiers.getEncrypted(), "encrypted", modList);
            resolveModifier(modifiers.getInitial(), "initial", modList);
            resolveModifier(modifiers.getOptional(), "optional", modList);
            resolveModifier(modifiers.getPartOf(), "partOf", modList);
            resolveModifier(modifiers.getPrivate(), "private", modList);
            resolveModifier(modifiers.getRead(), "read", modList);
            resolveModifier(modifiers.getRemovable(), "removable", modList);
            resolveModifier(modifiers.getSearch(), "search", modList);
            resolveModifier(modifiers.getUnique(), "unique", modList);
            resolveModifier(modifiers.getWrite(), "write", modList);
            if (!modList.isEmpty()) {
                return StringUtils.join(modList, ", ");
            }
        }
        return null;
    }

    private void resolveModifier(
        final GenericAttributeValue<Boolean> attribute,
        final String methodName,
        final List<String> resultList
    ) {
        if (attribute == null) {
            return;
        }
        final Boolean value = BooleanUtils.toBooleanObject(attribute.getStringValue());
        if (value == null) {
            return;
        }
        if (value) {
            resultList.add(methodName);
        } else {
            resultList.add(methodName + "(false)");
        }

    }

    private String localizeItemType(final DomElement dom) {
        if (dom instanceof ItemType) {
            ItemType itemType = (ItemType) dom;
            String value = resolveValue(itemType.getExtends());
            if (value == null) {
                value = "GenericItem";
            }
            return "(" + value + ")";
        }
        return null;
    }

    private String localizeAttribute(final DomElement dom) {
        if (dom instanceof Attribute) {
            final Attribute attribute = (Attribute) dom;
            String value = resolveValue(attribute.getType());
            if (value == null) {
                return null;
            }
            if (value.startsWith(TS_ATTRIBUTE_LOCALIZED_PREFIX)) {
                value = value.substring(TS_ATTRIBUTE_LOCALIZED_PREFIX.length());
            }
            if (value.startsWith("java.lang.")) {
                value = value.substring("java.lang.".length());
            }
            return value;
        }
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        final DomElement dom = getElement();
        if (dom instanceof Attribute) {
            final String value = resolveValue(((Attribute) dom).getType());
            if (StringUtils.startsWith(value, TS_ATTRIBUTE_LOCALIZED_PREFIX)) {
                return HybrisIcons.LOCALIZED;
            }
        }
        return null;
    }

}
