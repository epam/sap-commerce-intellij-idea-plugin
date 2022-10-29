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

package com.intellij.idea.plugin.hybris.linemarker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for show gutter icon for navigation between *-item.xml and generated classes.
 *
 * @author Nosov Aleksandr
 */
public class HybrisItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(
        @NotNull final PsiElement element,
        @NotNull final Collection<? super RelatedItemLineMarkerInfo<?>> result
    ) {
        if (!(element instanceof final PsiClass psiClass)) return;

        final String name = cleanSearchName(psiClass.getName());
        if (shouldProcessItemType(psiClass)) {
            Optional.ofNullable(TSMetaModelAccess.Companion.getInstance(psiClass.getProject()).findMetaItemByName(name))
                    .map(meta -> meta.retrieveAllDoms().stream()
                                     .map(DomElement::getXmlElement)
                                     .collect(Collectors.toList()))
                    .map(elements -> createTargetsWithGutterIcon(psiClass, elements))
                    .ifPresent(result::add);
        } else if (shouldProcessEnum(psiClass)) {
            Optional.ofNullable(TSMetaModelAccess.Companion.getInstance(psiClass.getProject()).findMetaEnumByName(name))
                    .map(meta -> meta.retrieveAllDoms().stream()
                                     .map(DomElement::getXmlElement)
                                     .collect(Collectors.toList()))
                    .map(elements -> createTargetsWithGutterIcon(psiClass, elements))
                    .ifPresent(result::add);
        }
    }

    private static String cleanSearchName(final String searchName) {
        if (searchName == null) return null;

        final int idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX);
        if (idx == -1) {
            return searchName;
        }
        return searchName.substring(0, idx);
    }

    private boolean shouldProcessItemType(final PsiClass psiClass) {
        return psiClass.getName() != null && psiClass.getName().endsWith(HybrisConstants.MODEL_SUFFIX)
               || (psiClass.getSuperClass() != null
                   && psiClass.getSuperClass().getName() != null
                   && psiClass.getSuperClass().getName().startsWith("Generated"));
    }

    private boolean shouldProcessEnum(final PsiClass psiClass) {
        final PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();

        for (final PsiClassType implementsListType : implementsListTypes) {
            if ("HybrisEnumValue".equals(implementsListType.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private @NotNull RelatedItemLineMarkerInfo<PsiElement> createTargetsWithGutterIcon(
        final PsiClass psiClass,
        final Collection<XmlElement> list
    ) {
        return NavigationGutterIconBuilder
            .create(HybrisIcons.TYPE_SYSTEM)
            .setTargets(list)
            .setEmptyPopupText(HybrisI18NBundleUtils.message("hybris.gutter.navigate.no.matching.beans"))
            .setPopupTitle(HybrisI18NBundleUtils.message("hybris.gutter.bean.class.navigate.choose.class.title"))
            .setTooltipText(HybrisI18NBundleUtils.message("hybris.gutter.item.class.tooltip.navigate.declaration"))
            .createLineMarkerInfo(Objects.requireNonNull(psiClass.getNameIdentifier()));
    }
}
