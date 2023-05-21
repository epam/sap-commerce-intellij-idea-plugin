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

package com.intellij.idea.plugin.hybris.codeInspection.fix.xml;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.PsiNavigateUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class XmlAddTagQuickFix implements LocalQuickFix {

    private final String fixName;
    private final String tagName;
    private final String tagBody;
    private SortedMap<String, String> attributes;

    // ? Maybe better to have tag name and before/after boolean?
    // ? Or maybe better to have different impl of XmlAddTagQuickFix and use factory?
    private final String insertAfterTag;

    public XmlAddTagQuickFix(
            final String tagName,
            final String tagBody,
            final SortedMap<String, String> attributes,
            final String insertAfterTag
    ) {
        this.fixName = getFixName(tagName, attributes);
        this.tagName = tagName;
        this.tagBody = tagBody;
        if (attributes != null) {
            this.attributes = Collections.unmodifiableSortedMap(attributes);
        } else {
            this.attributes = Collections.emptySortedMap();
        }
        this.insertAfterTag = insertAfterTag;
    }

    private static String getFixName(final String tagName, final SortedMap<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return HybrisI18NBundleUtils.message("hybris.inspections.fix.xml.AddTag", tagName);
        }

        return HybrisI18NBundleUtils.message(
            "hybris.inspections.fix.xml.AddTagWithAttributes",
            tagName,
            attributes.entrySet()
                      .stream()
                      .map(pair -> '\'' + pair.getKey() + "'='" + pair.getValue() + '\'')
                      .collect(Collectors.joining(", "))
        );
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return fixName;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        final PsiElement currentElement = descriptor.getPsiElement();

        if (currentElement instanceof XmlTag) {
            final XmlTag currentTag = (XmlTag) currentElement;

            // Create tag
            final XmlTag tagToInsert = currentTag.createChildTag(
                    tagName,
                    currentTag.getNamespace(),
                    tagBody,
                    false
            );

            // Insert tag
            final XmlTag insertedTag;
            if (StringUtils.isNotBlank(insertAfterTag)) {
                XmlTag subTag = currentTag.findFirstSubTag(insertAfterTag);
                insertedTag = (XmlTag) currentTag.addAfter(tagToInsert, subTag);
            } else {
                insertedTag = currentTag.addSubTag(tagToInsert, true);
            }

            // Add attributes
            attributes.forEach(insertedTag::setAttribute);

            // Change cursor position
            // ? Maybe there is a better default cursor placement than before end of tag?
            final ASTNode[] children = ((XmlTagImpl) insertedTag).getChildren(TokenSet.create(XmlTokenType.XML_END_TAG_START));
            if (children.length > 0) {
                navigateIfNotPreviewMode(descriptor, children[0].getPsi());
            } else {
                navigateIfNotPreviewMode(descriptor, insertedTag);
            }
        }
    }

    private void navigateIfNotPreviewMode(final ProblemDescriptor descriptor, final PsiElement psiElement) {
        if (descriptor instanceof ProblemDescriptorBase) {
            PsiNavigateUtil.navigate(psiElement);
        }
    }
}
