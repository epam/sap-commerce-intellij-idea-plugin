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

package com.intellij.idea.plugin.hybris.codeInsight.daemon;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation;
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomElement;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractTSAttributeLineMarkerProvider< T extends PsiElement> extends AbstractItemLineMarkerProvider<T> {

    @Override
    protected Optional<RelatedItemLineMarkerInfo<PsiElement>> collectDeclarations(final T psi) {
        if (!(psi.getParent() instanceof final PsiClass psiClass)) return Optional.empty();
        if (!ModelsUtils.isModelFile(psiClass)) return Optional.empty();

        final var project = psi.getProject();
        final var metaService = TSMetaModelAccess.Companion.getInstance(project);
        final var meta = metaService.findMetaItemByName(cleanSearchName(psiClass.getName()));

        if (meta == null) {
            return Optional.empty();
        }

        return collect(meta, psi);
    }

    protected abstract Optional<RelatedItemLineMarkerInfo<PsiElement>> collect(final TSGlobalMetaItem meta, final T psi);

    protected Optional<RelatedItemLineMarkerInfo<PsiElement>> getPsiElementRelatedItemLineMarkerInfo(
        final TSGlobalMetaItem meta, final String name, final PsiIdentifier nameIdentifier
    ) {
        final var elements = getAttributeElements(meta, name);
        if (elements.isEmpty()) {
            final var groupedRelElements = getRelations(meta, name);
            return getRelationMarkers(groupedRelElements, TSMetaRelation.RelationEnd.SOURCE,
                                      HybrisIcons.RELATION_SOURCE,
                                      nameIdentifier
            )
                .or(() -> getRelationMarkers(
                    groupedRelElements,
                    TSMetaRelation.RelationEnd.TARGET,
                    HybrisIcons.RELATION_TARGET, nameIdentifier
                ));
        } else {
            return Optional.of(createTargetsWithGutterIcon(nameIdentifier, elements, HybrisIcons.ATTRIBUTE));
        }
    }

    @Override
    protected String getTooltipText() {
        return HybrisI18NBundleUtils.message("hybris.editor.gutter.item.attribute.tooltip.navigate.declaration");
    }

    @Override
    protected String getPopupTitle() {
        return HybrisI18NBundleUtils.message("hybris.editor.gutter.bean.attribute.navigate.choose.class.title");
    }

    @Override
    protected String getEmptyPopupText() {
        return HybrisI18NBundleUtils.message("hybris.editor.gutter.navigate.no.matching.attributes");
    }

    private static List<XmlElement> getAttributeElements(final TSGlobalMetaItem meta, final String name) {
        return meta.getAllAttributes().stream()
                   .filter(attr -> attr.getName().equals(name))
                   .map(TSGlobalMetaItem.TSGlobalMetaItemAttribute::getDeclarations)
                   .flatMap(Collection::stream)
                   .map(TSMetaClassifier::getDomAnchor)
                   .map(DomAnchor::retrieveDomElement)
                   .filter(Objects::nonNull)
                   .map(DomElement::getXmlElement)
                   .toList();
    }

    private static Map<TSMetaRelation.RelationEnd, List<XmlElement>> getRelations(final TSGlobalMetaItem meta, final String name) {
        return meta.getAllRelationEnds().stream()
                   .filter(rel -> rel.getQualifier().equals(name))
                   .filter(TSMetaRelation.TSMetaRelationElement::isNavigable)
                   .filter(rel -> rel.getDomAnchor().retrieveDomElement() != null)
                   .collect(
                       Collectors.groupingBy(
                           TSMetaRelation.TSMetaRelationElement::getEnd,
                           Collectors.mapping(
                               rel -> Objects.requireNonNull(rel.getDomAnchor().retrieveDomElement()).getXmlElement(),
                               Collectors.toList()
                           )
                       )
                   );
    }

    private Optional<RelatedItemLineMarkerInfo<PsiElement>> getRelationMarkers(
        final Map<TSMetaRelation.RelationEnd, List<XmlElement>> groupedRelElements,
        final TSMetaRelation.RelationEnd target,
        final Icon icon, final PsiIdentifier nameIdentifier
    ) {
        return Optional.ofNullable(groupedRelElements.get(target))
                       .map(relElements -> createTargetsWithGutterIcon(nameIdentifier, relElements, icon));
    }
}
