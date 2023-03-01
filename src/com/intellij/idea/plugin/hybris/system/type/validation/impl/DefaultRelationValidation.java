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

package com.intellij.idea.plugin.hybris.system.type.validation.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.type.validation.ItemsXmlValidator;
import com.intellij.idea.plugin.hybris.system.type.model.Relation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.system.type.utils.TSUtils.getBoolean;
import static com.intellij.idea.plugin.hybris.system.type.utils.TSUtils.getString;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public class DefaultRelationValidation implements ItemsXmlValidator<Relation> {

    @Override
    public boolean validate(@Nullable final List<? extends Relation> dom, @NotNull final Map<String, ? extends PsiClass> psi) {
        if (null == dom) {
            return false;
        }

        final Map<String, PsiClass> filteredClasses = this.filterRelationClasses(psi, dom);

        for (final Relation relation : dom) {
            final boolean isOutOfDate = this.isRelationOutOfDate(relation, filteredClasses);
            if (isOutOfDate) {
                return true;
            }
        }

        return false;
    }

    private boolean isRelationOutOfDate(
        @NotNull final Relation relation,
        @NotNull final Map<String, PsiClass> filteredClasses
    ) {
        final Boolean sourceNavigable = getBoolean(relation.getSourceElement().getNavigable());

        if (Boolean.TRUE.equals(sourceNavigable)) {
            final Boolean isGenerate = getBoolean(relation.getSourceElement().getModel().getGenerate());

            if (Boolean.TRUE.equals(isGenerate)) {
                final String fieldNameInTarget = getString(relation.getSourceElement().getQualifier());
                final String targetClassName = getString(relation.getTargetElement().getType());

                if (this.isFieldNotPresentInClass(filteredClasses, targetClassName, fieldNameInTarget)) {
                    return true;
                }
            }
        }

        final Boolean targetNavigable = getBoolean(relation.getTargetElement().getNavigable());

        if (Boolean.TRUE.equals(targetNavigable)) {
            final Boolean isGenerate = getBoolean(relation.getTargetElement().getModel().getGenerate());

            if (Boolean.TRUE.equals(isGenerate)) {
                final String fieldNameInSource = getString(relation.getTargetElement().getQualifier());
                final String sourceClassName = getString(relation.getSourceElement().getType());

                return this.isFieldNotPresentInClass(filteredClasses, sourceClassName, fieldNameInSource);
            }
        }

        return false;
    }

    private boolean isFieldNotPresentInClass(
        @NotNull final Map<String, PsiClass> filteredClasses,
        @Nullable final String className,
        @Nullable final String fieldName
    ) {
        if (StringUtils.isAnyBlank(className, fieldName)) {
            return false;
        }

        final PsiClass classItem = filteredClasses.get(className);

        if (null == classItem) {
            return true;
        }

        for (final PsiField classField : classItem.getAllFields()) {
            if (StringUtils.endsWithIgnoreCase(classField.getName(), fieldName)) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    private Map<String, PsiClass> filterRelationClasses(
        @NotNull final Map<String, ? extends PsiClass> generatedClasses,
        @NotNull final Collection<? extends Relation> relationsList
    ) {
        final Map<String, PsiClass> filteredClasses = new CaseInsensitiveMap<>();

        if (CollectionUtils.isEmpty(relationsList)) {
            return filteredClasses;
        }

        final Set<String> relationClasses = this.getRelationClassNames(relationsList);

        for (final String relationClass : relationClasses) {
            if (!filteredClasses.containsKey(relationClass)) {
                final PsiClass psiClass = generatedClasses.get(relationClass + HybrisConstants.MODEL_SUFFIX);

                if (null != psiClass) {
                    filteredClasses.put(relationClass, psiClass);
                }
            }
        }

        return filteredClasses;
    }

    @NotNull
    private Set<String> getRelationClassNames(@NotNull final Collection<? extends Relation> relationsList) {
        final Set<String> sourceRelationClasses = relationsList.stream().map(
            relation -> getString(relation.getSourceElement().getType())
        ).collect(Collectors.toSet());

        final Set<String> targetRelationClasses = relationsList.stream().map(
            relation -> getString(relation.getTargetElement().getType())
        ).collect(Collectors.toSet());

        final Set<String> relationClasses = new HashSet<>();
        relationClasses.addAll(sourceRelationClasses);
        relationClasses.addAll(targetRelationClasses);

        return relationClasses;
    }
}
