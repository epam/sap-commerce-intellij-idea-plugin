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

package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaDeployment;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode;
import com.intellij.idea.plugin.hybris.type.system.model.Index;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaItemImpl extends TSMetaEntityImpl<ItemType> implements TSMetaItem {

    private final NoCaseMultiMap<TSMetaItemAttribute> myAttributes = new NoCaseMultiMap<>();
    private final NoCaseMultiMap<TSMetaCustomProperty> myCustomProperties = new NoCaseMultiMap<>();
    private final NoCaseMultiMap<TSMetaItemIndex> myIndexes = new NoCaseMultiMap<>();
    private final Set<DomAnchor<ItemType>> myAllDoms = new LinkedHashSet<>();

    private final TSMetaDeployment<TSMetaItem> myDeployment;
    private String myExtendedMetaItemName;
    private final boolean myAbstract;
    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final boolean mySingleton;
    private final boolean myJaloOnly;
    private final String myJaloClass;
    private final String myDescription;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public TSMetaItemImpl(final Module module, final Project project, final String name, final @NotNull ItemType dom, final boolean custom) {
        super(module, project, name, dom, custom);
        myAllDoms.add(DomService.getInstance().createAnchor(dom));
        registerExtends(dom);
        myAbstract = Boolean.TRUE.equals(dom.getAbstract().getValue());
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        mySingleton = Boolean.TRUE.equals(dom.getSingleton().getValue());
        myJaloOnly = Boolean.TRUE.equals(dom.getJaloOnly().getValue());
        myJaloClass = dom.getJaloClass().getStringValue();
        myDescription = Optional.ofNullable(dom.getDescription().getXmlTag())
                                .map(description -> description.getValue().getText())
                                .orElse(null);
        myDeployment = new TSMetaDeploymentImpl<>(module, project, this, dom.getDeployment(), custom);
    }

    @NotNull
    @Override
    public Stream<? extends ItemType> retrieveAllDomsStream() {
        return myAllDoms.stream()
                        .map(DomAnchor::retrieveDomElement)
                        .filter(Objects::nonNull);
    }

    @Override
    public void addAttribute(final String key, final TSMetaItemAttribute attribute) {
        myAttributes.putValue(key, attribute);
    }

    @Override
    public void addCustomProperty(final String key, final TSMetaCustomProperty customProperty) {
        myCustomProperties.putValue(key, customProperty);
    }

    @Override
    public void addIndex(final String key, final TSMetaItemIndex index) {
        myIndexes.putValue(key, index);
    }

    @Override
    public NoCaseMultiMap<TSMetaItemAttribute> getAttributes() {
        return myAttributes;
    }

    @Override
    public NoCaseMultiMap<TSMetaCustomProperty> getCustomAttributes() {
        return myCustomProperties;
    }

    @Override
    public NoCaseMultiMap<TSMetaItemIndex> getIndexes() {
        return myIndexes;
    }

    @Nullable
    @Override
    public String getExtendedMetaItemName() {
        return myExtendedMetaItemName;
    }

    @Override
    public void merge(final TSMetaItem another) {
        addDomRepresentation(another.retrieveDom());
    }

    @Override
    public TSMetaDeployment<TSMetaItem> getDeployment() {
        return myDeployment;
    }

    protected void addDomRepresentation(final @NotNull ItemType anotherDom) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom));
        registerExtends(anotherDom);
    }

    private void registerExtends(final @NotNull ItemType dom) {
        //only one extends is allowed
        if (myExtendedMetaItemName == null) {
            myExtendedMetaItemName = dom.getExtends().getRawText();
        }
    }

    @Override
    public boolean isAbstract() {
        return myAbstract;
    }

    @Override
    public boolean isAutoCreate() {
        return myAutoCreate;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }

    @Override
    public boolean isSingleton() {
        return mySingleton;
    }

    @Override
    public boolean isJaloOnly() {
        return myJaloOnly;
    }

    @Override
    public String getJaloClass() {
        return myJaloClass;
    }

    @Override
    public String getDescription() {
        return myDescription;
    }

    public static class TSMetaItemIndexImpl extends TSMetaEntityImpl<Index> implements TSMetaItemIndex {

        private final TSMetaItem myOwner;
        private final boolean myRemove;
        private final boolean myReplace;
        private final boolean myUnique;
        private final Set<String> myKeys;
        private final CreationMode myCreationMode;

        public TSMetaItemIndexImpl(final Module module, final Project project, final @NotNull TSMetaItem owner, final @NotNull Index dom, final boolean custom) {
            super(module, project, extractName(dom), dom, custom);
            myOwner = owner;
            myRemove = Boolean.TRUE.equals(dom.getRemove().getValue());
            myReplace = Boolean.TRUE.equals(dom.getReplace().getValue());
            myUnique = Boolean.TRUE.equals(dom.getUnique().getValue());
            myCreationMode = Optional.ofNullable(dom.getCreationMode().getValue()).orElse(CreationMode.ALL);
            myKeys = dom.getKeys().stream()
                        .map(indexKey -> indexKey.getAttribute().getStringValue())
                        .collect(Collectors.toSet());
        }

        @Nullable
        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public Set<String> getKeys() {
            return Collections.unmodifiableSet(myKeys);
        }

        @Nullable
        private static String extractName(final Index dom) {
            return dom.getName().getStringValue();
        }

        @Override
        public boolean isRemove() {
            return myRemove;
        }

        @Override
        public boolean isReplace() {
            return myReplace;
        }

        @Override
        public boolean isUnique() {
            return myUnique;
        }

        @Override
        public CreationMode getCreationMode() {
            return myCreationMode;
        }

        @Override
        public TSMetaItem getOwner() {
            return myOwner;
        }
    }

    public static class TSMetaItemAttributeImpl extends TSMetaEntityImpl<Attribute> implements TSMetaItemAttribute {

        private final TSMetaItem myOwner;
        private final NoCaseMultiMap<TSMetaCustomProperty> myCustomProperties = new NoCaseMultiMap<>();
        private final boolean myDeprecated;
        private final boolean myAutoCreate;
        private final boolean myGenerate;
        private final boolean myRedeclare;
        private final String myDescription;
        private final String myDefaultValue;
        @Nullable private final String myType;

        public TSMetaItemAttributeImpl(final Module module, final Project project, final @NotNull TSMetaItem owner, final @NotNull Attribute dom, final boolean custom) {
            super(module, project, extractName(dom), dom, custom);
            myOwner = owner;
            myDeprecated = extractDeprecated(dom);
            myRedeclare = Boolean.TRUE.equals(dom.getRedeclare().getValue());
            myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
            myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
            myType = dom.getType().getStringValue();
            myDescription = Optional.ofNullable(dom.getDescription().getXmlTag())
                .map(xmlTag -> xmlTag.getValue().getText())
                .orElse(null);
            myDefaultValue = dom.getDefaultValue().getStringValue();
            dom.getCustomProperties().getProperties().stream()
                    .map(domAttribute -> new TSMetaCustomPropertyImpl(module, project, domAttribute, custom))
                    .filter(attribute -> StringUtils.isNotBlank(attribute.getName()))
                    .forEach(attribute -> addCustomProperty(attribute.getName().trim(), attribute));
        }

        @Override
        @Nullable
        public String getType() {
            return myType;
        }

        @Override
        public boolean isDeprecated() {
            return myDeprecated;
        }

        @Override
        public boolean isAutoCreate() {
            return myAutoCreate;
        }

        @Override
        public boolean isRedeclare() {
            return myRedeclare;
        }

        @Override
        public boolean isGenerate() {
            return myGenerate;
        }

        @Override
        public void addCustomProperty(final String key, final TSMetaCustomProperty customProperty) {
            myCustomProperties.putValue(key, customProperty);
        }

        @Override
        public @NotNull List<? extends TSMetaCustomProperty> getCustomProperties(final boolean includeInherited) {
            return new LinkedList<>(myCustomProperties.values());
        }

        @Nullable
        @Override
        public String getName() {
            return super.getName();
        }

        @Nullable
        @Override
        public String getDescription() {
            return myDescription;
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return myDefaultValue;
        }

        @NotNull
        @Override
        public TSMetaItem getOwner() {
            return myOwner;
        }

        @Nullable
        private static String extractName(final Attribute dom) {
            return dom.getQualifier().getStringValue();
        }

        private boolean extractDeprecated(@NotNull final Attribute dom) {
            final String name = getName();
            return name != null && dom.getModel().getSetters().stream().anyMatch(
                setter -> name.equals(setter.getName().getStringValue()) &&
                          Boolean.TRUE.equals(setter.getDeprecated().getValue()));
        }

    }
}
