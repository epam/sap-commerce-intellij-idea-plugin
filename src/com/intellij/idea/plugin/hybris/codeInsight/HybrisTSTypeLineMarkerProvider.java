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

package com.intellij.idea.plugin.hybris.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.DomElement;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for show gutter icon for navigation between *-item.xml and generated classes.
 *
 * @author Nosov Aleksandr
 */
public class HybrisTSTypeLineMarkerProvider extends AbstractHybrisItemLineMarkerProvider<PsiClass> {

    @Override
    protected boolean canProcess(final PsiElement psi) {
        return psi instanceof PsiClass;
    }

    @Override
    protected Optional<RelatedItemLineMarkerInfo<PsiElement>> collectDeclarations(
        final PsiClass psi
    ) {
        final var name = cleanSearchName(psi.getName());
        final var psiNameIdentifier = psi.getNameIdentifier();

        if (ModelsUtils.isModelFile(psi)) {
            return Optional.ofNullable(TSMetaModelAccess.Companion.getInstance(psi.getProject()).findMetaItemByName(name))
                           .map(meta -> meta.retrieveAllDoms().stream()
                                            .map(DomElement::getXmlElement)
                                            .collect(Collectors.toList()))
                           .map(elements -> createTargetsWithGutterIcon(psiNameIdentifier, elements, HybrisIcons.TYPE_SYSTEM));
        } else if (ModelsUtils.isEnumFile(psi)) {
            return Optional.ofNullable(TSMetaModelAccess.Companion.getInstance(psi.getProject()).findMetaEnumByName(name))
                           .map(meta -> meta.retrieveAllDoms().stream()
                                            .map(DomElement::getXmlElement)
                                            .collect(Collectors.toList()))
                           .map(elements -> createTargetsWithGutterIcon(psiNameIdentifier, elements, HybrisIcons.TYPE_SYSTEM));
        }
        return Optional.empty();
    }

    @Override
    protected String getTooltipText() {
        return HybrisI18NBundleUtils.message("hybris.gutter.item.class.tooltip.navigate.declaration");
    }

    @Override
    protected String getPopupTitle() {
        return HybrisI18NBundleUtils.message("hybris.gutter.bean.type.navigate.choose.class.title");
    }

    @Override
    protected String getEmptyPopupText() {
        return HybrisI18NBundleUtils.message("hybris.gutter.navigate.no.matching.beans");
    }
}
