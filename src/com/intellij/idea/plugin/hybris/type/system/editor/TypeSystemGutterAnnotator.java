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

package com.intellij.idea.plugin.hybris.type.system.editor;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TypeSystemGutterAnnotator implements Annotator {

    @Override
    public void annotate(
        @NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder
    ) {
        if (!(psiElement instanceof XmlAttributeValue) ||
            !TypeSystemUtils.isTypeSystemXmlFile(psiElement.getContainingFile())) {
            return;
        }
        final XmlTag parentTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);

        if (parentTag == null) {
            return;
        }
        final DomElement dom = DomManager.getDomManager(parentTag.getProject()).getDomElement(parentTag);

        if (!(dom instanceof ItemType)) {
            return;
        }

        final ItemType itemType = (ItemType) dom;
        if (!psiElement.equals(itemType.getCode().getXmlAttributeValue())) {
            return;
        }

        final Collection<? extends PsiElement> alternativeDoms = findAlternativeDoms(itemType);
        if (!alternativeDoms.isEmpty()) {
            NavigationGutterIconBuilder
                // no recalculation for alternative doms
                .create(AllIcons.Actions.Forward, it -> alternativeDoms)
                .setTarget(itemType)
                .setTooltipText(alternativeDoms.size() > 1
                                    ? HybrisI18NBundleUtils.message("hybris.editor.typesystem.alternativeDefinitions")
                                    : HybrisI18NBundleUtils.message("hybris.editor.typesystem.alternativeDefinition"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createGutterIcon(annotationHolder, psiElement);
        }

        final Optional<TSMetaItem> firstExtender = findFirstExtendingMetaClass(itemType);

        if (firstExtender.isPresent()) {
            NavigationGutterIconBuilder
                .create(AllIcons.Gutter.OverridenMethod, this::findAllExtendingXmlAttributes)
                .setTarget(itemType)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTooltipText("Has subtypes")
                .createGutterIcon(annotationHolder, psiElement);
        }
    }

    @NotNull
    private Collection<XmlAttributeValue> findAlternativeDoms(@NotNull final ItemType source) {
        final String code = source.getCode().getStringValue();

        if (StringUtil.isEmpty(code)) {
            return Collections.emptyList();
        }
        final XmlElement element = source.getXmlElement();
        final PsiFile psiFile = element == null ? null : element.getContainingFile();

        if (psiFile == null) {
            return Collections.emptyList();
        }

        final TSMetaItem metaItem = TSMetaModelAccess.Companion.getInstance(psiFile.getProject()).findMetaItemForDom(source);

        if (metaItem == null) {
            return Collections.emptyList();
        }

        return Optional.of(metaItem)
                       .map(TSMetaItem::retrieveAllDomsStream)
                       .orElse(Stream.empty())
                       .filter(dom -> !dom.equals(source))
                       .map(ItemType::getCode)
                       .sorted(compareByModuleName())
                       .map(GenericAttributeValue::getXmlAttributeValue)
                       .collect(Collectors.toList());
    }

    @NotNull
    private Optional<TSMetaItem> findFirstExtendingMetaClass(@NotNull final ItemType source) {
        return getExtendingMetaItemsNames(source).stream().findAny();
    }

    @NotNull
    private Collection<PsiElement> findAllExtendingXmlAttributes(@NotNull final ItemType source) {
        return getExtendingMetaItemsNames(source).stream()
                                                 .flatMap(TSMetaItem::retrieveAllDomsStream)
                                                 .map(ItemType::getCode)
                                                 .sorted(compareByModuleName())
                                                 .map(GenericAttributeValue::getXmlAttributeValue)
                                                 .filter(Objects::nonNull)
                                                 .collect(Collectors.toList());
    }

    @NotNull
    private List<TSMetaItem> getExtendingMetaItemsNames(@NotNull final ItemType source) {
        final String code = source.getCode().getStringValue();

        if (StringUtil.isEmpty(code)) {
            return Collections.emptyList();
        }
        final XmlElement xmlElement = source.getXmlElement();
        final PsiFile psiFile = xmlElement == null ? null : xmlElement.getContainingFile();

        if (psiFile == null) {
            return Collections.emptyList();
        }
        final TSMetaModelAccess metaService = TSMetaModelAccess.Companion.getInstance(psiFile.getProject());
        final TSMetaItem metaItem = metaService.findMetaItemForDom(source);

        if (metaItem == null) {
            return Collections.emptyList();
        }

        return metaService.<TSMetaItem>getAll(MetaType.META_ITEM).stream()
                          .filter(meta -> metaItem.getName().equals(meta.getExtendedMetaItemName()))
                          .collect(Collectors.toList());
    }

    @NotNull
    private static Comparator<GenericAttributeValue<String>> compareByModuleName() {
        return (o1, o2) -> Comparing.compare(o1.getModule(), o2.getModule(), (m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()));
    }


}
