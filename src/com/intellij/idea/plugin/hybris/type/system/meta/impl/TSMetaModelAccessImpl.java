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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TSMetaModelAccessImpl implements TSMetaModelAccess {

    public static final Key<TSMetaModel> META_MODEL_CACHE_KEY = Key.create("META_MODEL_CACHE_KEY");
    private static final Logger LOG = Logger.getInstance(TSMetaModelAccessImpl.class);

    private final Project myProject;
    private final ParameterizedCachedValue<TSMetaModel, TSMetaModel> myMetaModel;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public TSMetaModelAccessImpl(@NotNull final Project project) {
        myProject = project;

        myMetaModel = CachedValuesManager.getManager(project).createParameterizedCachedValue(
            param -> ApplicationManager.getApplication().runReadAction(
                (Computable<CachedValueProvider.Result<TSMetaModel>>) () -> {
                    final Object[] dependencies = new TSMetaModelBuilder(myProject).collectFiles()
                                                                                   .stream()
                                                                                   .filter(Objects::nonNull)
                                                                                   .toArray();
                    LOG.warn("Type System Cache COMPLETED - " + Thread.currentThread().getId());
                    return CachedValueProvider.Result.create(param, dependencies.length == 0 ? ModificationTracker.EVER_CHANGED : dependencies);
                }), false);
    }

    @Override
    public TSMetaModel getMetaModel() {
        if (myMetaModel.hasUpToDateValue() || lock.isWriteLocked()) {
            // parameter not needed, we have to pass new cache holder only during write process
            final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

            try {
                readLock.lock();
                return myMetaModel.getValue(null);
            } finally {
                readLock.unlock();
            }
        }

        final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        try {
            // we have to put and remove new cache from the user data here due fact that process can be cancelled and cached object may stay in obsolete state
            writeLock.lock();
            LOG.warn("Type System Cache STARTED - " + Thread.currentThread().getId());

            final TSMetaModel newMetaModel = new TSMetaModel();
            myProject.putUserData(META_MODEL_CACHE_KEY, newMetaModel);

            return myMetaModel.getValue(newMetaModel);
        } finally {
            // reset user data cache, it is required only for new Meta Cache proper creation
            myProject.putUserData(META_MODEL_CACHE_KEY, null);
            writeLock.unlock();
        }
    }


}
