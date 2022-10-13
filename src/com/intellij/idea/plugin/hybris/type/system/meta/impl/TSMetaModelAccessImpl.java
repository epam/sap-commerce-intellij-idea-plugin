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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Global Meta Model can be retrieved at any time and will ensure that only single Thread can perform its initialization/update
 * <p>
 * Main idea is that we have two levels of Meta Model cache:
 *  1. Global Meta Model cached at Project level with dependencies to all items.xml files in the Project.
 *      - processing of the dependant PsiFiles is ignored and done during retrieval from the PsiFile cache
 *      - once all dependant PsiFiles processed, each Meta Model will be merged into single one
 *  2. PsiFile (items.xml) specific cache
 *      - retrieving of that cache also performs processing of the PsiFile and pre-filling into MetaModel caches
 */
public class TSMetaModelAccessImpl implements TSMetaModelAccess {

    private static final Key<ParameterizedCachedValue<TSMetaModel, TSMetaModel>> SINGLE_MODEL_CACHE_KEY = Key.create("SINGLE_TS_MODEL_CACHE");
    public static final Key<TSMetaModel> GLOBAL_META_MODEL_CACHE_KEY = Key.create("META_MODEL_CACHE_KEY");

    private final Project myProject;
    private final ParameterizedCachedValue<TSMetaModel, TSMetaModel> myGlobalMetaModel;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public TSMetaModelAccessImpl(@NotNull final Project project) {
        myProject = project;

        myGlobalMetaModel = CachedValuesManager.getManager(project).createParameterizedCachedValue(
            param -> ApplicationManager.getApplication().runReadAction(
                (Computable<CachedValueProvider.Result<TSMetaModel>>) () -> {
                    myProject.putUserData(GLOBAL_META_MODEL_CACHE_KEY, param);
                    final Object[] dependencies = TSMetaModelBuilder.prepare(myProject).collectDependencies().stream()
                                                                    .filter(Objects::nonNull)
                                                                    .toArray();

                    return CachedValueProvider.Result.create(param, dependencies.length == 0 ? ModificationTracker.EVER_CHANGED : dependencies);
                }), false);

    }

    @Override
    public TSMetaModel getMetaModel() {
        return myGlobalMetaModel.hasUpToDateValue() || lock.isWriteLocked()
            ? readMetaModelWithLock()
            : writeMetaModelWithLock();
    }

    // parameter for meta model cached value is not required, we have to pass new cache holder only during write process
    private TSMetaModel readMetaModelWithLock() {
        final var readLock = lock.readLock();

        try {
            readLock.lock();
            return myGlobalMetaModel.getValue(null);
        } finally {
            readLock.unlock();
        }
    }

    private TSMetaModel writeMetaModelWithLock() {
        final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        try {
            // we have to put and remove new cache from the user data here due fact that process can be cancelled and cached object may stay in obsolete state
            writeLock.lock();
            return DumbService.getInstance(myProject).runReadActionInSmartMode(new Computable<TSMetaModel>() {

                @Override
                public TSMetaModel compute() {
                    final var globalMetaModel = new TSMetaModel();

                    TSMetaModelBuilder.prepare(myProject).collectDependencies()
                                      .stream()
                                      .filter(Objects::nonNull)
                                      .map(TSMetaModelAccessImpl.this::retrieveSingleMetaModelPerFile)
                                      .map(TSMetaModelAccessImpl.this::retrieveSingleMetaModel)
                                      .forEach(globalMetaModel::merge);

                    return myGlobalMetaModel.getValue(globalMetaModel);
                }
            });
        } finally {
            // reset user data cache, it is required only for new Meta Cache proper creation
            myProject.putUserData(GLOBAL_META_MODEL_CACHE_KEY, null);
            writeLock.unlock();
        }
    }

    private TSMetaModel retrieveSingleMetaModel(final ParameterizedCachedValue<TSMetaModel, TSMetaModel> cached) {
        if (cached.hasUpToDateValue()) {
            return cached.getValue(null);
        } else {
            try {
                return cached.getValue(new TSMetaModel());
            } finally {
                // reset user data cache, it is required only for new Meta Cache proper creation
                myProject.putUserData(GLOBAL_META_MODEL_CACHE_KEY, null);
            }
        }
    }

    @NotNull
    private ParameterizedCachedValue<TSMetaModel, TSMetaModel> retrieveSingleMetaModelPerFile(final PsiFile psiFile) {
        return Optional.ofNullable(psiFile.getUserData(SINGLE_MODEL_CACHE_KEY))
                       .orElseGet(() -> {
                           final var cachedValue = createSingleMetaModelCachedValue(myProject, psiFile);
                           psiFile.putUserData(SINGLE_MODEL_CACHE_KEY, cachedValue);

                           return cachedValue;
                       });
    }

    private ParameterizedCachedValue<TSMetaModel, TSMetaModel> createSingleMetaModelCachedValue(
        final @NotNull Project project,
        final @NotNull PsiFile psiFile
    ) {
        return CachedValuesManager.getManager(project).createParameterizedCachedValue(
            param -> ApplicationManager.getApplication().runReadAction(
                (Computable<CachedValueProvider.Result<TSMetaModel>>) () -> {
                    myProject.putUserData(GLOBAL_META_MODEL_CACHE_KEY, param);
                    TSMetaModelBuilder.prepare(myProject).process(psiFile);

                    return CachedValueProvider.Result.create(param, psiFile);
                }), false);
    }

}
