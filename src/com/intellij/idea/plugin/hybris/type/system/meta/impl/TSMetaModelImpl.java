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

import com.intellij.idea.plugin.hybris.type.system.inspections.XmlRuleInspection;
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAtomic;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCollection;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.idea.plugin.hybris.common.utils.CollectionUtils.emptyCollectionIfNull;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TSMetaModelImpl implements TSMetaModel {

    private NoCaseMultiMap<TSMetaReference.ReferenceEnd> myReferencesBySourceTypeName;
    private final List<TSMetaModelImpl> myBaseModels;

    public TSMetaModelImpl() {
        this(Collections.emptyList());
    }

    public TSMetaModelImpl(@NotNull final Collection<TSMetaModelImpl> baseModels) {
        myBaseModels = new ArrayList<>(baseModels);
    }

    @NotNull
    private synchronized NoCaseMultiMap<TSMetaReference.ReferenceEnd> getReferencesBySourceTypeName() {
        if (myReferencesBySourceTypeName == null) {
            myReferencesBySourceTypeName = new NoCaseMultiMap<>();
            myBaseModels.forEach(model -> myReferencesBySourceTypeName.putAllValues(model.getReferencesBySourceTypeName()));
        }
        return myReferencesBySourceTypeName;
    }

    @Nullable
    TSMetaClassImpl findOrCreateClass(final @NotNull ItemType domItemType) {
        final String name = TSMetaClassImpl.extractMetaClassName(domItemType);
        if (name == null) {
            return null;
        }
        final String typeCode = domItemType.getDeployment().getTypeCode().getStringValue();
        final CaseInsensitiveMap<String, TSMetaClassImpl> classes = XmlRuleInspection.getMetaType(MetaType.META_CLASS);
        TSMetaClassImpl impl = classes.get(name);
        if (impl == null) {
            impl = new TSMetaClassImpl(this, name, typeCode, domItemType);
            classes.put(name, impl);
        } else {
            impl.addDomRepresentation(domItemType);
        }
        return impl;
    }

    @Nullable
    TSMetaEnumImpl findOrCreateEnum(final @NotNull EnumType domEnumType) {
        final String name = TSMetaEnumImpl.extractName(domEnumType);
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        final CaseInsensitiveMap<String, TSMetaEnumImpl> enums = XmlRuleInspection.getMetaType(MetaType.META_ENUM);
        TSMetaEnumImpl impl = enums.get(name);
        if (impl == null) {
            impl = new TSMetaEnumImpl(name, domEnumType);
            enums.put(name, impl);
        } else {
            //report a problem
        }
        return impl;
    }

    @Nullable
    TSMetaCollectionImpl findOrCreateCollection(@NotNull final CollectionType domCollectionType) {
        final String name = TSMetaCollectionImpl.extractName(domCollectionType);
        if (StringUtil.isEmpty(name)) {
            return null;
        }

        return XmlRuleInspection.<TSMetaCollectionImpl>getMetaType(MetaType.META_COLLECTION)
                                .computeIfAbsent(name, key -> new TSMetaCollectionImpl(key, domCollectionType));
    }

    @Nullable
    TSMetaReference findOrCreateReference(@NotNull final Relation domRelationType) {
        final String name = TSMetaReferenceImpl.extractName(domRelationType);
        if (StringUtil.isEmpty(name)) {
            return null;
        }

        return XmlRuleInspection.<TSMetaReference>getMetaType(MetaType.META_RELATION)
                                .computeIfAbsent(name, key -> {
                                    final String typeCode = domRelationType.getDeployment().getTypeCode().getStringValue();
                                    final TSMetaReference impl = new TSMetaReferenceImpl(name, typeCode, domRelationType);
                                    registerReferenceEnd(impl.getSource(), impl.getTarget());
                                    registerReferenceEnd(impl.getTarget(), impl.getSource());
                                    return impl;
                                });
    }

    private void registerReferenceEnd(
        @NotNull final TSMetaReference.ReferenceEnd ownerEnd,
        @NotNull final TSMetaReference.ReferenceEnd targetEnd
    ) {
        if (!targetEnd.isNavigable()) {
            return;
        }
        final String ownerTypeName = ownerEnd.getTypeName();
        if (!StringUtil.isEmpty(ownerTypeName)) {
            getReferencesBySourceTypeName().putValue(ownerTypeName, targetEnd);
        }
    }

    void collectReferencesForSourceType(
        final @NotNull TSMetaClassImpl source,
        final @NotNull Collection<TSMetaReference.ReferenceEnd> out
    ) {

        out.addAll(getReferencesBySourceTypeName().get(source.getName()));
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaClass> getMetaClassesStream() {
        return XmlRuleInspection.<TSMetaClass>getMetaType(MetaType.META_CLASS).values().stream();
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaReference> getMetaRelationsStream() {
        return XmlRuleInspection.<TSMetaReference>getMetaType(MetaType.META_RELATION).values().stream();
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaAtomic> getMetaAtomicStream() {
        return XmlRuleInspection.<TSMetaAtomic>getMetaType(MetaType.META_ATOMIC).values().stream();
    }

    @Nullable
    @Override
    public TSMetaClass findMetaClassByName(@NotNull final String name) {
        return XmlRuleInspection.<TSMetaClass>getMetaType(MetaType.META_CLASS).get(name);
    }

    @Nullable
    @Override
    public TSMetaClass findMetaClassForDom(@NotNull final ItemType dom) {
        return Optional.ofNullable(TSMetaClassImpl.extractMetaClassName(dom))
                       .map(this::findMetaClassByName)
                       .orElse(null);
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaEnum> getMetaEnumsStream() {
        return XmlRuleInspection.<TSMetaEnum>getMetaType(MetaType.META_ENUM).values().stream();
    }

    @Nullable
    @Override
    public TSMetaEnum findMetaEnumByName(@NotNull final String name) {
        return XmlRuleInspection.<TSMetaEnum>getMetaType(MetaType.META_ENUM).get(name);
    }

    @Nullable
    @Override
    public TSMetaAtomic findMetaAtomicByName(@NotNull final String name) {
        return XmlRuleInspection.<TSMetaAtomic>getMetaType(MetaType.META_ATOMIC).get(name);
    }

    @Nullable
    @Override
    public List<TSMetaReference> findRelationByName(@NotNull final String name) {
        return emptyCollectionIfNull(getReferencesBySourceTypeName().values()).stream()
                                           .filter(Objects::nonNull)
                                           .map(TSMetaReference.ReferenceEnd::getOwningReference)
                                           .filter(ref -> ref.getName().equals(name))
                                           .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaCollection> getMetaCollectionsStream() {
        return XmlRuleInspection.<TSMetaCollection>getMetaType(MetaType.META_COLLECTION).values().stream();
    }

    @Nullable
    @Override
    public TSMetaCollection findMetaCollectionByName(@NotNull final String name) {
        return XmlRuleInspection.<TSMetaCollection>getMetaType(MetaType.META_COLLECTION).get(name);
    }

    @Nullable
    @Override
    public TSMetaAtomic findOrCreateAtomicType(@NotNull final AtomicType atomicType) {
        final String clazzName = atomicType.getClazz().getValue();

        return XmlRuleInspection.<TSMetaAtomic>getMetaType(MetaType.META_ATOMIC)
                                .computeIfAbsent(clazzName, key -> new TSMetaAtomicImpl(key, atomicType));
    }
}
