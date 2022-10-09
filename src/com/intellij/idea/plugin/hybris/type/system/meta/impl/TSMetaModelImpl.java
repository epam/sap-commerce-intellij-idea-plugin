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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAtomic;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCollection;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMap;
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap;
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType;
import com.intellij.idea.plugin.hybris.type.system.model.CollectionType;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.idea.plugin.hybris.common.utils.CollectionUtils.emptyCollectionIfNull;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TSMetaModelImpl implements TSMetaModel {

    private enum MetaType {
        META_CLASS, META_REFERENCE, META_ENUM, META_COLLECTION, META_ATOMIC
    }

    private NoCaseMap<TSMetaClassImpl> myClasses;
    private NoCaseMap<TSMetaReferenceImpl> myRelations;
    private NoCaseMap<TSMetaEnumImpl> myEnums;
    private NoCaseMap<TSMetaCollectionImpl> myCollections;
    private NoCaseMap<TSMetaAtomicImpl> myAtomics;
    private NoCaseMultiMap<TSMetaReference.ReferenceEnd> myReferencesBySourceTypeName;
    private final List<TSMetaModelImpl> myBaseModels;

    private static final Map<MetaType, NoCaseMap<TSMetaClassifier<? extends DomElement>>> CACHE = new ConcurrentHashMap<>();

    public TSMetaModelImpl() {
        this(Collections.emptyList());
    }

    public TSMetaModelImpl(@NotNull final Collection<TSMetaModelImpl> baseModels) {
        myBaseModels = new ArrayList<>(baseModels);
    }

    @NotNull
    private synchronized NoCaseMap<TSMetaClassImpl> getClasses() {
        if (myClasses == null) {
            myClasses = new NoCaseMap<>();
            myBaseModels.forEach(model -> myClasses.putAll(model.getClasses()));
        }
        return myClasses;
    }

    @NotNull
    private synchronized NoCaseMap<TSMetaReferenceImpl> getRelations() {
        if (myRelations == null) {
            myRelations = new NoCaseMap<>();
            myBaseModels.forEach(model -> myRelations.putAll(model.getRelations()));
        }
        return myRelations;
    }

    @NotNull
    private synchronized NoCaseMap<TSMetaEnumImpl> getEnums() {
        if (myEnums == null) {
            myEnums = new NoCaseMap<>();
            myBaseModels.forEach(model -> myEnums.putAll(model.getEnums()));
        }
        return myEnums;
    }

    @NotNull
    private synchronized NoCaseMap<TSMetaCollectionImpl> getCollections() {
        if (myCollections == null) {
            myCollections = new NoCaseMap<>();
            myBaseModels.forEach(model -> myCollections.putAll(model.getCollections()));
        }
        return myCollections;
    }

    @NotNull
    private synchronized NoCaseMap<TSMetaAtomicImpl> getAtomics() {
        if (myAtomics == null) {
            myAtomics = new NoCaseMap<>();
            myBaseModels.forEach(model -> myAtomics.putAll(model.getAtomics()));
        }
        return myAtomics;
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
    TSMetaClassImpl findOrCreateClass(final TSMetaModelImpl own, final @NotNull ItemType domItemType) {
        final String name = TSMetaClassImpl.extractMetaClassName(domItemType);
        if (name == null) {
            return null;
        }
        final String typeCode = domItemType.getDeployment().getTypeCode().getStringValue();
        final NoCaseMap<TSMetaClassImpl> classes = getClasses();
        TSMetaClassImpl impl = classes.get(name);
        if (impl == null) {
            impl = new TSMetaClassImpl(own, name, typeCode, domItemType);
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
        final NoCaseMap<TSMetaEnumImpl> enums = getEnums();
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
    TSMetaCollectionImpl findOrCreateCollection(final TSMetaModelImpl own, @NotNull final CollectionType domCollectionType) {
        final String name = TSMetaCollectionImpl.extractName(domCollectionType);
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        final NoCaseMap<TSMetaCollectionImpl> collections = getCollections();
        TSMetaCollectionImpl impl = collections.get(name);
        if (impl == null) {
            impl = new TSMetaCollectionImpl(own, name, domCollectionType);
            collections.put(name, impl);
        }
        return impl;
    }

    @Nullable
    TSMetaReference findOrCreateReference(final TSMetaModelImpl owner, @NotNull final Relation domRelationType) {
        final String name = TSMetaReferenceImpl.extractName(domRelationType);
        if (StringUtil.isEmpty(name)) {
            return null;
        }

        final NoCaseMap<TSMetaReferenceImpl> relations = getRelations();
        final String typeCode = domRelationType.getDeployment().getTypeCode().getStringValue();

        TSMetaReferenceImpl impl = relations.get(name);
        if (impl == null) {
            impl = new TSMetaReferenceImpl(owner, name, typeCode, domRelationType);
            registerReferenceEnd(impl.getSource(), impl.getTarget());
            registerReferenceEnd(impl.getTarget(), impl.getSource());
            relations.put(name, impl);
        }
        return impl;
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

    @Override
    public void collectReferencesForSourceType(
        final @NotNull TSMetaClassImpl source,
        final @NotNull Collection<TSMetaReference.ReferenceEnd> out
    ) {
        out.addAll(getReferencesBySourceTypeName().get(source.getName()));
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaClass> getMetaClassesStream() {
        return getClasses().values().stream();
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaReference> getMetaRelationsStream() {
        return getRelations().values().stream();
    }

    @NotNull
    @Override
    public Stream<? extends TSMetaAtomic> getMetaAtomicStream() {
        return getAtomics().values().stream();
    }

    @Nullable
    @Override
    public TSMetaClass findMetaClassByName(@NotNull final String name) {
        return getClasses().get(name);
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
        return getEnums().values().stream();
    }

    @Nullable
    @Override
    public TSMetaEnum findMetaEnumByName(@NotNull final String name) {
        return getEnums().get(name);
    }

    @Nullable
    @Override
    public TSMetaAtomic findMetaAtomicByName(@NotNull final String name) {
        return getAtomics().get(name);
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
        return getCollections().values().stream();
    }

    @Nullable
    @Override
    public TSMetaCollection findMetaCollectionByName(@NotNull final String name) {
        return getCollections().get(name);
    }

    @Nullable
    @Override
    public TSMetaAtomic findOrCreateAtomicType(@NotNull final AtomicType atomicType) {
        final String clazzName = atomicType.getClazz().getValue();
        final NoCaseMap<TSMetaAtomicImpl> atomics = getAtomics();
        final TSMetaAtomicImpl tsMetaAtomic = atomics.get(clazzName);
        if (tsMetaAtomic == null) {
            TSMetaAtomic atomic = new TSMetaAtomicImpl(clazzName, atomicType);
            atomics.put(clazzName, (TSMetaAtomicImpl) atomic);
            return atomic;
        }
        return tsMetaAtomic;
    }
}
