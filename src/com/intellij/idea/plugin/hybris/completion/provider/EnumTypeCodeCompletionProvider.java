/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.meta.model.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaEnum;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnumTypeCodeCompletionProvider extends CompletionProvider<CompletionParameters> {

    @NotNull
    public static CompletionProvider<CompletionParameters> getInstance() {
        return ApplicationManager.getApplication().getService(EnumTypeCodeCompletionProvider.class);
    }

    @Override
    public void addCompletions(
        @NotNull final CompletionParameters parameters,
        @NotNull final ProcessingContext context,
        @NotNull CompletionResultSet result
    ) {
        final Project project = getProject(parameters);

        if (project == null) {
            return;
        }
        result = result.caseInsensitive();

        TSMetaModelAccess.Companion.getInstance(project).<TSGlobalMetaEnum>getAll(MetaType.META_ENUM).stream()
                                   .filter(meta -> meta.getName() != null)
                                   .map(meta -> LookupElementBuilder.create(meta.getName()).withIcon(HybrisIcons.TYPE_SYSTEM))
                                   .forEach(result::addElement);
    }

    @Nullable
    private Project getProject(final @NotNull CompletionParameters parameters) {
        Validate.notNull(parameters);

        return parameters.getEditor().getProject();
    }
}
