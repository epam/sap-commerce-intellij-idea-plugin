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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor;
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase.TSResolveResult;
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.system.type.model.Attribute;
import com.intellij.idea.plugin.hybris.system.type.model.Index;
import com.intellij.idea.plugin.hybris.system.type.model.Indexes;
import com.intellij.idea.plugin.hybris.system.type.model.ItemType;
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ImpexUniqueAttributeWithoutIndexInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
        @NotNull final ProblemsHolder holder, final boolean isOnTheFly
    ) {
        return new ParameterChecker(holder);
    }

    private static class ParameterChecker extends ImpexVisitor {

        private final ProblemsHolder myHolder;

        public ParameterChecker(@NotNull final ProblemsHolder holder) {
            myHolder = holder;
        }

        @Override
        public void visitFullHeaderParameter(@NotNull final ImpexFullHeaderParameter param) {
            ImpexAttribute impexAttribute = findUniqueAttribute(param);
            if (impexAttribute == null) {
                return;
            }
            Attribute domAttribute = resolveToAttribute(param.getAnyHeaderParameterName());
            if (domAttribute == null) {
                return;
            }
            if (!isCustomAttribute(domAttribute)) {
                return;
            }
            String attributeName = domAttribute.getQualifier().getStringValue();
            if (StringUtil.isEmptyOrSpaces(attributeName)) {
                return;
            }
            ItemType domType = DomUtil.getParentOfType(domAttribute, ItemType.class, true);
            if (domType == null || !domType.exists()) {
                return;
            }
            if (hasIndexForAttribute(domType, attributeName)) {
                return;
            }

            myHolder.registerProblem(
                param,
                MessageFormat.format("Attribute ''{0}'' does not have an index", attributeName)
            );
        }

        private boolean isCustomAttribute(final Attribute attribute) {
            return Optional.ofNullable(attribute)
                           .map(DomElement::getXmlElement)
                           .map(XmlElement::getContainingFile)
                           .map(TSUtils::isCustomExtensionFile)
                           .orElse(false);
        }

        private boolean hasIndexForAttribute(
            @NotNull final ItemType domItemType,
            @NotNull final String attributeName
        ) {
            if (hasLocalIndexForAttribute(domItemType, attributeName)) {
                return true;
            }
            //it also may be in the separate representation

            final TSGlobalMetaItem merged = TSMetaModelAccess.Companion.getInstance(myHolder.getProject()).findMetaForDom(domItemType);
            return merged != null && merged.retrieveAllDoms().stream()
                                           .filter(it -> !domItemType.equals(it))
                                           .anyMatch(it -> hasLocalIndexForAttribute(it, attributeName));
        }

        private static boolean hasLocalIndexForAttribute(
            @NotNull final ItemType domItemType,
            @NotNull final String attributeName
        ) {
            final Indexes indexes = domItemType.getIndexes();
            return indexes.exists() &&
                   indexes.getIndexes().stream()
                          .anyMatch(idx -> isIndexForAttribute(idx, attributeName));
        }

        private static boolean isIndexForAttribute(
            @NotNull final Index index,
            @NotNull final String attributeName
        ) {
            return index.exists() &&
                   index.getKeys().size() == 1 &&
                   attributeName.equals(index.getKeys().get(0).getAttribute().getStringValue());
        }

        private static ImpexAttribute findUniqueAttribute(@NotNull final ImpexFullHeaderParameter param) {
            return param.getModifiersList().stream()
                        .flatMap(modifier -> modifier.getAttributeList().stream())
                        .filter(attr -> hasAttributeValue(attr, "true"))
                        .filter(attr -> hasAttributeName(attr, "unique"))
                        .findAny()
                        .orElse(null);
        }

        private static boolean hasAttributeValue(@NotNull final ImpexAttribute attr, @NotNull String text) {
            ImpexAnyAttributeValue value = attr.getAnyAttributeValue();
            return value != null && text.equals(value.getText());
        }

        private static boolean hasAttributeName(@NotNull final ImpexAttribute attr, @NotNull String name) {
            ImpexAnyAttributeName attrName = attr.getAnyAttributeName();
            return attrName.getStringList().isEmpty() &&
                   attrName.getFirstChild() != null &&
                   attrName.getFirstChild() == attrName.getLastChild() &&
                   name.equals(attrName.getText());
        }

        @Nullable
        private static Attribute resolveToAttribute(@NotNull final ImpexAnyHeaderParameterName name) {
            return Arrays.stream(name.getReferences())
                         .map(ref -> ObjectUtils.tryCast(ref, PsiPolyVariantReference.class))
                         .filter(Objects::nonNull)
                         .flatMap(ref -> Arrays.stream(ref.multiResolve(false)))
                         .map(resolve -> ObjectUtils.tryCast(resolve, TSResolveResult.class))
                         .filter(Objects::nonNull)
                         .map(TSResolveResult::getSemanticDomElement)
                         .filter(Objects::nonNull)
                         .map(dom -> ObjectUtils.tryCast(dom, Attribute.class))
                         .filter(Objects::nonNull)
                         .findAny()
                         .orElse(null);
        }
    }
}
