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
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TSMetaModelAccessImpl implements TSMetaModelAccess {

    private static final Key<CachedValue<TSMetaModelImpl>> EXTERNAL_MODEL_CACHE_KEY = Key.create(
        "EXTERNAL_TS_MODEL_CACHE");
    private static final Key<CachedValue<TSMetaModelImpl>> FILE_MODEL_CACHE_KEY = Key.create("FILE_TS_MODEL_CACHE");

    private final CachedValue<TSMetaModel> myCachedValue;

    public TSMetaModelAccessImpl(@NotNull final Project project) {
        myCachedValue = CachedValuesManager.getManager(project).createCachedValue(
            () -> ApplicationManager.getApplication().runReadAction(
                (Computable<CachedValueProvider.Result<TSMetaModel>>) () -> {

                    final TSMetaModelBuilder builder = new TSMetaModelBuilder(project);
                    final TSMetaModelImpl model = builder.buildModel();
                    final Object[] dependencies = builder.getFiles().stream()
                                                         .filter(Objects::nonNull)
                                                         .toArray();
                    return CachedValueProvider.Result.create(model, dependencies.length == 0 ? ModificationTracker.EVER_CHANGED: dependencies);
                }), false);
    }

    @Override
    public synchronized TSMetaModel getTypeSystemMeta(@Nullable final PsiFile contextFile) {
        if (contextFile == null || !TypeSystemUtils.isTsFile(contextFile)) {
            return myCachedValue.getValue();
        }
        final TSMetaModelImpl externalModel = doGetExternalModel(contextFile);
        final Project project = contextFile.getProject();
        CachedValue<TSMetaModelImpl> fileModelCache = contextFile.getUserData(FILE_MODEL_CACHE_KEY);

        if (fileModelCache == null) {
            fileModelCache = CachedValuesManager.getManager(project).createCachedValue(
                () -> ApplicationManager.getApplication().runReadAction(
                    (Computable<CachedValueProvider.Result<TSMetaModelImpl>>) () -> {

                        final TSMetaModelBuilder builder = new TSMetaModelBuilder(project);
                        final TSMetaModelImpl modelForFile = builder.buildModelForFile(contextFile);
                        return CachedValueProvider.Result.create(modelForFile, contextFile);

                    }), false);
            contextFile.putUserData(FILE_MODEL_CACHE_KEY, fileModelCache);
        }
        final TSMetaModelImpl fileModel = fileModelCache.getValue();
        return new TSMetaModelImpl(Arrays.asList(externalModel, fileModel));
    }

    @Override
    public synchronized TSMetaModel getExternalTypeSystemMeta(@NotNull final PsiFile contextFile) {
        return TypeSystemUtils.isTsFile(contextFile) ? doGetExternalModel(contextFile) : myCachedValue.getValue();
    }

    @NotNull
    private TSMetaModelImpl doGetExternalModel(final @NotNull PsiFile contextFile) {
        final PsiFile originalFile = contextFile.getOriginalFile();
        final VirtualFile vFile = originalFile.getVirtualFile();
        final Project project = originalFile.getProject();
        CachedValue<TSMetaModelImpl> externalModelCache = originalFile.getUserData(EXTERNAL_MODEL_CACHE_KEY);

        if (externalModelCache == null) {

            externalModelCache = CachedValuesManager.getManager(project).createCachedValue(
                () -> ApplicationManager.getApplication().runReadAction(
                    (Computable<CachedValueProvider.Result<TSMetaModelImpl>>) () -> {

                        final List<VirtualFile> excludes = vFile == null
                            ? Collections.emptyList()
                            : Collections.singletonList(vFile);

                        final TSMetaModelBuilder builder = new TSMetaModelBuilder(project, excludes);
                        final TSMetaModelImpl model = builder.buildModel();
                        return CachedValueProvider.Result.create(model, builder.getFiles());

                    }), false);
            originalFile.putUserData(EXTERNAL_MODEL_CACHE_KEY, externalModelCache);
        }
        return externalModelCache.getValue();
    }

    @Override
    @NotNull
    public TSMetaModel getTypeSystemMeta() {
        return getTypeSystemMeta(null);
    }

}
