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

package com.intellij.idea.plugin.hybris.impex.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.CODE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.NAME_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.SOURCE_ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.TARGET_ATTRIBUTE_NAME;

/**
 * Created 22:13 14 May 2016
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class ImpexHeaderItemTypeAttributeNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @NotNull
    public static CompletionProvider<CompletionParameters> getInstance() {
        return ApplicationManager.getApplication().getService(ImpexHeaderItemTypeAttributeNameCompletionProvider.class);
    }

    @Override
    public void addCompletions(
        @NotNull final CompletionParameters parameters,
        final ProcessingContext context,
        @NotNull final CompletionResultSet result
    ) {
        Validate.notNull(parameters);
        Validate.notNull(result);

        final Project project = this.getProject(parameters);
        if (null == project) {
            return;
        }

        final PsiElement psiElementUnderCaret = parameters.getPosition();
        final ImpexHeaderTypeName headerTypeName = this.getHeaderTypeNamePsiElementOfAttribute(psiElementUnderCaret);

        if (headerTypeName != null) {
            fillDomAttributesCompletions(project, headerTypeName, result);
        }
    }

    protected void fillDomAttributesCompletions(
        @NotNull final Project project,
        @NotNull final ImpexHeaderTypeName headerTypeName,
        @NotNull final CompletionResultSet resultSet
    ) {
        final var metaService = TSMetaModelAccess.Companion.getInstance(project);
        final var typeCode = headerTypeName.getText();

        Optional.ofNullable(metaService.findMetaItemByName(typeCode))
            .map(ImpexHeaderItemTypeAttributeNameCompletionProvider::getCompletions)
            .or(() -> Optional.ofNullable(metaService.findMetaEnumByName(typeCode))
                              .map(ImpexHeaderItemTypeAttributeNameCompletionProvider::getCompletions))
            .or(() -> Optional.ofNullable(metaService.findMetaRelationByName(typeCode))
                              .map(metaRelation -> ImpexHeaderItemTypeAttributeNameCompletionProvider.getCompletions(metaRelation, metaService)))
            .ifPresent(resultSet::addAllElements);
    }

    private static List<LookupElementBuilder> getCompletions(final TSGlobalMetaEnum meta) {
        return List.of(
            LookupElementBuilder.create(CODE_ATTRIBUTE_NAME)
                                .withIcon(HybrisIcons.ATTRIBUTE)
                                .withTypeText("String", true),
            LookupElementBuilder.create(NAME_ATTRIBUTE_NAME)
                                .withIcon(HybrisIcons.ATTRIBUTE)
                                .withTypeText("String", true)
        );
    }

    private static List<LookupElementBuilder> getCompletions(final TSGlobalMetaRelation metaRelation, final TSMetaModelAccess metaService) {
        final var linkMetaItem = metaService.findMetaItemByName("Link");
        if (linkMetaItem == null) return Collections.emptyList();

        final var completions = new LinkedList<>(getCompletions(linkMetaItem, Set.of(SOURCE_ATTRIBUTE_NAME, TARGET_ATTRIBUTE_NAME)));

        completions.add(
            LookupElementBuilder.create(SOURCE_ATTRIBUTE_NAME)
                .withIcon(HybrisIcons.RELATION_SOURCE)
                .withStrikeoutness(metaRelation.getSource().isDeprecated())
                .withTypeText(metaRelation.getSource().getFlattenType(), true)
        );
        completions.add(
            LookupElementBuilder.create(TARGET_ATTRIBUTE_NAME)
                .withIcon(HybrisIcons.RELATION_TARGET)
                .withStrikeoutness(metaRelation.getTarget().isDeprecated())
                .withTypeText(metaRelation.getTarget().getFlattenType(), true)
        );

        return completions;
    }

    private static List<LookupElementBuilder> getCompletions(final TSGlobalMetaItem metaItem) {
        return getCompletions(metaItem, Collections.emptySet());
    }

    private static List<LookupElementBuilder> getCompletions(final TSGlobalMetaItem metaItem, final Set<String> excludeNames) {
        final var attributes = metaItem.getAllAttributes().stream()
                                       .map(attribute -> mapAttributeToLookup(excludeNames, attribute))
                                       .filter(Objects::nonNull);

        final var relationEnds = metaItem.getAllRelationEnds().stream()
            .map(ref -> LookupElementBuilder.create(ref.getQualifier())
                                            .withIcon(
                                                switch (ref.getEnd()) {
                                                    case SOURCE -> HybrisIcons.RELATION_SOURCE;
                                                    case TARGET -> HybrisIcons.RELATION_TARGET;
                                                }
                                            )
                                            .withTypeText(ref.getFlattenType(), true)
            );

        return Stream.concat(attributes, relationEnds).collect(Collectors.toList());
    }

    @Nullable
    private static LookupElementBuilder mapAttributeToLookup(
        final Set<String> excludeNames,
        final TSGlobalMetaItem.TSGlobalMetaItemAttribute attribute
    ) {
        final var name = attribute.getName();

        if (StringUtils.isBlank(name) || excludeNames.contains(name.trim())) {
            return null;
        }
        return LookupElementBuilder.create(name.trim())
            .withIcon(HybrisIcons.ATTRIBUTE)
            .withTailText(attribute.isDynamic() ? " (" + HybrisI18NBundleUtils.message("hybris.ts.type.dynamic") + ')' : "", true)
            .withStrikeoutness(attribute.isDeprecated())
            .withTypeText(attribute.getFlattenType(), true);
    }

    @Nullable
    private Project getProject(final @NotNull CompletionParameters parameters) {
        Validate.notNull(parameters);

        return parameters.getEditor().getProject();
    }

    @Nullable
    @Contract("null -> null")
    protected ImpexHeaderTypeName getHeaderTypeNamePsiElementOfAttribute(@Nullable final PsiElement headerAttributePsiElement) {
        if (null == headerAttributePsiElement || null == headerAttributePsiElement.getNode()) {
            return null;
        }

        final ImpexHeaderLine impexHeaderLine = PsiTreeUtil.getParentOfType(
            headerAttributePsiElement,
            ImpexHeaderLine.class
        );

        if (null == impexHeaderLine) {
            return null;
        }

        final ImpexFullHeaderType impexFullHeaderType = impexHeaderLine.getFullHeaderType();

        return null == impexFullHeaderType ? null : impexFullHeaderType.getHeaderTypeName();
    }
}
