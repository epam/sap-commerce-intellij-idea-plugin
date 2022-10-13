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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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

        final TSMetaModelService metaService = TSMetaModelService.Companion.getInstance(project);
        final String itemTypeCode = headerTypeName.getText();
        final Optional<TSMetaItem> metaItem = Optional.ofNullable(metaService.findMetaItemByName(itemTypeCode));

        metaItem
            .map(meta -> meta.getProperties(true).stream())
            .orElse(Stream.empty())
            .map(prop -> {
                final String name = prop.getName();

                if (StringUtils.isBlank(name)) {
                    return null;
                }
                final LookupElementBuilder builder = LookupElementBuilder
                    .create(name.trim())
                    .withIcon(HybrisIcons.TYPE_SYSTEM)
                    .withStrikeoutness(prop.isDeprecated());
                final String typeText = getTypePresentableText(prop.getType());
                return StringUtil.isEmpty(typeText) ? builder : builder.withTypeText(typeText, true);
            })
            .filter(Objects::nonNull)
            .forEach(resultSet::addElement);

        metaItem
            .map(meta -> meta.getReferenceEndsStream(true))
            .orElse(Stream.empty())
            .map(ref -> LookupElementBuilder.create(ref.getRole()).withIcon(HybrisIcons.TYPE_SYSTEM))
            .forEach(resultSet::addElement);
    }

    @NotNull
    private static String getTypePresentableText(@Nullable final String type) {
        if (type == null) {
            return "";
        }
        final int index = type.lastIndexOf('.');
        return index >= 0 ? type.substring(index + 1) : type;
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
