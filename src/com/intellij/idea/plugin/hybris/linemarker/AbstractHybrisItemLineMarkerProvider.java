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
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class AbstractHybrisItemLineMarkerProvider<T extends PsiElement> extends RelatedItemLineMarkerProvider {

    protected static String cleanSearchName(final String searchName) {
        if (searchName == null) {
            return null;
        }

        final int idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX);
        if (idx == -1) {
            return searchName;
        }
        return searchName.substring(0, idx);
    }

    @Override
    protected void collectNavigationMarkers(
        @NotNull final PsiElement element,
        @NotNull final Collection<? super RelatedItemLineMarkerInfo<?>> result
    ) {
        if (!canProcess(element)) return;

        final var module = ModuleUtil.findModuleForPsiElement(element);
        if (module == null || !isPlatformModule(module)) return;

        collectDeclarations((T) element)
            .ifPresent(result::add);
    }

    private static boolean isPlatformModule(final @NotNull Module module) {
        return Objects.equals(HybrisModuleDescriptor.getDescriptorType(module), HybrisModuleDescriptorType.PLATFORM);
    }

    protected abstract boolean canProcess(final PsiElement psi);

    protected abstract Optional<RelatedItemLineMarkerInfo<PsiElement>> collectDeclarations(final T psi);

    protected @NotNull RelatedItemLineMarkerInfo<PsiElement> createTargetsWithGutterIcon(
        final PsiIdentifier psiIdentifier,
        final Collection<XmlElement> list,
        final Icon icon
    ) {
        return NavigationGutterIconBuilder
            .create(icon)
            .setTargets(list)
            .setEmptyPopupText(getEmptyPopupText())
            .setPopupTitle(getPopupTitle())
            .setTooltipText(getTooltipText())
            .createLineMarkerInfo(psiIdentifier);
    }

    protected abstract String getEmptyPopupText();

    protected abstract String getPopupTitle();

    protected abstract String getTooltipText();

}
