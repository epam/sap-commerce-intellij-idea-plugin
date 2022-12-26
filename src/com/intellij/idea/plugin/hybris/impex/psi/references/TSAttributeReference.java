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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.AttributeResolveResult;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationElementResolveResult;
import com.intellij.idea.plugin.hybris.psi.references.TSReferenceBase;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaEnum;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.CODE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.NAME_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.SOURCE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TARGET_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.findHeaderItemTypeName;


/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TSAttributeReference extends TSReferenceBase<ImpexAnyHeaderParameterName> {

    public TSAttributeReference(@NotNull final ImpexAnyHeaderParameterNameMixin owner) {
        super(owner);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator == null || indicator.isCanceled()) {
            return ResolveResult.EMPTY_ARRAY;
        }
        final TSMetaModelAccess metaModelAccess = getMetaModelAccess();
        final TSMetaItemService metaItemService = getMetaItemService();
        final String featureName = getElement().getText().trim();

        List<? extends ResolveResult> result = tryResolveForItemType(metaModelAccess, metaItemService, featureName);

        if (result == null) {
            result = tryResolveForRelationType(metaModelAccess, metaItemService, featureName);
        }

        if (result == null) {
            result = tryResolveForEnumType(metaModelAccess, featureName);
        }

        if (result == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return result.toArray(new ResolveResult[0]);
    }

    private List<EnumResolveResult> tryResolveForEnumType(final TSMetaModelAccess metaService, final String featureName) {
        return findHeaderItemTypeName(getElement())
            .map(PsiElement::getText)
            .map(metaService::findMetaEnumByName)
            .filter(it -> CODE_ATTRIBUTE_NAME.equals(featureName) || NAME_ATTRIBUTE_NAME.equals(featureName))
            .map(TSMetaEnum::retrieveDom)
            .map(EnumResolveResult::new)
            .map(Collections::singletonList)
            .orElse(null);
    }

    private List<ResolveResult> tryResolveForItemType(final TSMetaModelAccess meta,
                                                      final TSMetaItemService metaItemService,
                                                      final String featureName) {
        final Optional<TSGlobalMetaItem> metaItem = findHeaderItemTypeName(getElement()).map(PsiElement::getText)
                                                                                        .map(meta::findMetaItemByName);
        if (metaItem.isEmpty()) {
            return null;
        }

        final List<ResolveResult> result = resolveMetaItemAttributes(metaItemService, featureName, metaItem.get());

        metaItemService.findRelationEndsByQualifier(metaItem.get(), featureName, true)
                       .stream()
                       .map(TSMetaRelation.TSMetaRelationElement::retrieveDom)
                       .filter(Objects::nonNull)
                       .map(RelationElementResolveResult::new)
                       .collect(Collectors.toCollection(() -> result));

        return result;
    }

    private List<ResolveResult> tryResolveForRelationType(final TSMetaModelAccess metaService, final TSMetaItemService metaItemService, final String featureName) {
        return findHeaderItemTypeName(getElement())
            .map(PsiElement::getText)
            .map(metaService::findMetaRelationByName)
            .<List<ResolveResult>>map(meta -> {
                if (SOURCE_ATTRIBUTE_NAME.equalsIgnoreCase(featureName)) {
                    final var dom = meta.getSource().retrieveDom();
                    return dom != null
                        ? Collections.singletonList(new RelationElementResolveResult(dom))
                        : null;
                } else if (TARGET_ATTRIBUTE_NAME.equalsIgnoreCase(featureName)) {
                    final var dom = meta.getTarget().retrieveDom();
                    return dom != null
                        ? Collections.singletonList(new RelationElementResolveResult(dom))
                        : null;
                }

                return Optional.ofNullable(metaService.findMetaItemByName("Link"))
                    .map(metaLink -> resolveMetaItemAttributes(metaItemService, featureName, metaLink))
                    .orElse(null);
            })
            .orElse(null);
    }

    private static List<ResolveResult> resolveMetaItemAttributes(
        final TSMetaItemService metaItemService,
        final String featureName,
        final TSGlobalMetaItem metaItem
    ) {
        return metaItemService.findAttributesByName(metaItem, featureName, true).stream()
                              .map(TSGlobalMetaItem.TSGlobalMetaItemAttribute::retrieveDom)
                              .filter(Objects::nonNull)
                              .map(AttributeResolveResult::new)
                              .collect(Collectors.toCollection(LinkedList::new));
    }

}
