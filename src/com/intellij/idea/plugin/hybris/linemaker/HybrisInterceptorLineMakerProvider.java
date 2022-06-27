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

package com.intellij.idea.plugin.hybris.linemaker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Class for show gutter icon for navigation between model classes and interceptors.
 *
 * @author Andrei Kudarenko
 */
public class HybrisInterceptorLineMakerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(
        @NotNull final PsiElement element,
        @NotNull final Collection<? super RelatedItemLineMarkerInfo<?>> result
    ) {
        if (element instanceof PsiClass) {
            final PsiClass psiClass = (PsiClass) element;
            final Project project = element.getProject();

            if (isNotHybrisProject(project)) {
                return;
            }

            if (isModelClass(psiClass)) {
                final List<PsiClass> interceptors = getPsiClassInterceptors(psiClass, project);
                if (CollectionUtils.isNotEmpty(interceptors)) {
                    createTargetsWithGutterIcon(result, psiClass, interceptors);
                }
            }
        }
    }

    @NotNull
    private List<PsiClass> getPsiClassInterceptors(final PsiClass psiClass, final Project project) {
        final SmartPsiElementPointer<PsiClass> interceptorClass = findInterceptorClass(project);
        if (interceptorClass != null) {
            final Query<PsiClass> allInterceptors = ClassInheritorsSearch.search(Objects.requireNonNull(interceptorClass.getElement()));
            return allInterceptors.findAll().stream()
                                  .filter(interceptorPsiClass -> filterByGeneric(
                                      interceptorPsiClass,
                                      psiClass.getName()
                                  ))
                                  .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean isModelClass(final PsiClass psiClass) {
        final String psiClassName = psiClass.getName();
        return StringUtils.endsWith(psiClassName, "Model") ||
               (psiClass.getSuperClass() != null &&
                psiClass.getSuperClass().getName() != null &&
                psiClass.getSuperClass().getName().startsWith("Generated"));
    }

    private boolean isNotHybrisProject(@NotNull final Project project) {
        return !HybrisProjectSettingsComponent.getInstance(project).getState().isHybrisProject();
    }

    private boolean filterByGeneric(final PsiClass interceptorPsiClass, final String modelClassName) {
        final PsiClassType[] interfaces = interceptorPsiClass.getImplementsListTypes();
        if (interfaces.length > 0) {
            for (final PsiClassType anInterface : interfaces) {
                for (PsiType parameter : anInterface.getParameters()) {
                    if (Objects.equals(modelClassName, ((PsiClassReferenceType) parameter).getName())) {
                        return true;
                    }
                }
            }
        }

        final PsiClass superClass = interceptorPsiClass.getSuperClass();
        if (superClass != null && !StringUtils.equals(superClass.getName(), "Object")) {
            return filterByGeneric(superClass, modelClassName);
        }
        return false;
    }

    private SmartPsiElementPointer<PsiClass> findInterceptorClass(final Project project) {
        final PsiClass interceptorClass = JavaPsiFacade.getInstance(project).findClass(
            "de.hybris.platform.servicelayer.interceptor.Interceptor",
            GlobalSearchScope.allScope(project)
        );
        if (interceptorClass != null) {
            return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(interceptorClass);
        }
        return null;
    }

    private void createTargetsWithGutterIcon(
        final Collection<? super RelatedItemLineMarkerInfo<?>> result,
        final PsiClass psiClass,
        final Collection<PsiClass> list
    ) {
        final NavigationGutterIconBuilder<PsiElement> builder
            = NavigationGutterIconBuilder.create(HybrisIcons.INTERCEPTOR).setTargets(list);

        builder.setEmptyPopupText(HybrisI18NBundleUtils.message(
            "hybris.gutter.navigate.no.matching.interceptors"
        ));

        builder.setPopupTitle(HybrisI18NBundleUtils.message(
            "hybris.gutter.interceptor.class.navigate.choose.class.title"
        ));
        builder.setTooltipText(HybrisI18NBundleUtils.message(
            "hybris.gutter.interceptor.class.tooltip.navigate.declaration"
        ));
        final PsiIdentifier nameIdentifier = psiClass.getNameIdentifier();
        final RelatedItemLineMarkerInfo<?> lineMarkerInfo = builder.createLineMarkerInfo(nameIdentifier);
        result.add(lineMarkerInfo);
    }

}
