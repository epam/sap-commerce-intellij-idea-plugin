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

package com.intellij.idea.plugin.hybris.impex.psi.references;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.ItemResolveResult;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationResolveResult;
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaItem;
import com.intellij.idea.plugin.hybris.system.type.model.EnumType;
import com.intellij.idea.plugin.hybris.system.type.model.Relation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TSItemReference extends TSReferenceBase<ImpexHeaderTypeName> {

    public TSItemReference(@NotNull final ImpexHeaderTypeName owner) {
        super(owner);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null && indicator.isCanceled()) return ResolveResult.EMPTY_ARRAY;

        final var cachedResolveResult = getElement().getUserData(ImpexHeaderTypeNameMixin.CACHE_KEY);
        if (cachedResolveResult != null) return cachedResolveResult;

        final var lookingForName = getValue();
        final var metaService = getMetaModelAccess();

        final var resolvedResults = Optional.ofNullable(metaService.findMetaItemByName(lookingForName))
                                            .map(TSItemReference::resolveItem)
                                            .or(() -> Optional.ofNullable(metaService.findMetaEnumByName(lookingForName))
                                                              .map(TSGlobalMetaEnum::retrieveDom)
                                                              .map(TSItemReference::resolveEnum))
                                            .or(() -> Optional.ofNullable(metaService.findMetaRelationByName(lookingForName))
                                                              .map(TSGlobalMetaRelation::retrieveDom)
                                                              .map(TSItemReference::resolveRelation))
                                            .orElse(ResolveResult.EMPTY_ARRAY);
        getElement().putUserData(ImpexHeaderTypeNameMixin.CACHE_KEY, resolvedResults);
        return resolvedResults;
    }

    @Override
    public @Nullable PsiElement resolve() {
        final var resolveResults = multiResolve(false);
        if (resolveResults.length != 1) return null;

        final var result = resolveResults[0];
        if (!result.isValidResult()) return null;

        return result.getElement();
    }

    private static ResolveResult[] resolveItem(final TSGlobalMetaItem metaItem) {
        return metaItem.getDeclarations().stream()
                       .map(TSMetaItem::retrieveDom)
                       .filter(Objects::nonNull)
                       .map(ItemResolveResult::new)
                       .toArray(ResolveResult[]::new);
    }

    private static ResolveResult[] resolveEnum(final EnumType enumType) {
        final var resolveResult = new EnumResolveResult(enumType);
        return new ResolveResult[]{resolveResult};
    }

    private static ResolveResult[] resolveRelation(final Relation relationType) {
        final var resolveResult = new RelationResolveResult(relationType);
        return new ResolveResult[]{resolveResult};
    }

}
