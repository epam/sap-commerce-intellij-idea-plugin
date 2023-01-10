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

package com.intellij.idea.plugin.hybris.common.utils;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiFilePattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * @author Aleksandr Nosov <nosovae.dev@gmail.com>
 */
public class PsiXmlUtils {

    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern tagAttributeValuePatternInFile(
        String tagName,
        String attributeName,
        String fileName
    ) {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName(tagName)
                    )
            ).inside(
                insideTagPattern(tagName)
            ).inFile(PlatformPatterns.psiFile()
                                .withName(StandardPatterns.string().endsWith(fileName + ".xml")));
    }
    
    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern tagAttributeValuePatternInFile(
        String attributeName,
        String fileName
    ) {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                    )
            ).inside(
                PlatformPatterns.psiElement(XmlTag.class)
            ).inFile(PlatformPatterns.psiFile()
                                .withName(StandardPatterns.string().endsWith(fileName + ".xml")));
    }
    
    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern tagAttributeValuePattern(
        String tagName,
        String attributeName,
        String rootTag
    ) {
        return XmlPatterns
            .xmlAttributeValue()
            .withAncestor(6, xmlTag().withLocalName(rootTag))
            .withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        xmlTag()
                            .withName(tagName)
                    )        
            )
            .inside(
                insideTagPattern(tagName)
            );
    }
    
    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern tagAttributeValuePattern(
        String attributeName,
        String rootTag
    ) {
        return XmlPatterns
            .xmlAttributeValue()
            .withAncestor(6, xmlTag().withLocalName(rootTag))
            .and(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        xmlTag()
                    )
            ).inside(
                PlatformPatterns.psiElement(XmlTag.class)
            ));
    }

    public static PsiFilePattern.Capture<PsiFile> getXmlFilePattern(@Nullable String fileName) {
        if (fileName == null) {
            return getXmlFilePattern();
        }

        return PlatformPatterns.psiFile()
                          .withName(StandardPatterns.string().equalTo(fileName + ".xml"));
    }

    public static PsiFilePattern.Capture<PsiFile> getXmlFilePattern() {
        return PlatformPatterns.psiFile()
                          .withName(StandardPatterns.string().endsWith(".xml"));
    }

    public static XmlAttributeValuePattern attributeValuePattern(final String tagName, final String attributeName) {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        xmlTag()
                            .withName(tagName)
                    )
            );
    }

    public static PsiElementPattern.Capture<XmlTag> insideTagPattern(String insideTagName) {
        return PlatformPatterns.psiElement(XmlTag.class).withName(insideTagName);
    }

    public static PsiElementPattern.Capture<PsiElement> tagAttributePattern(
        String tag,
        String attributeName,
        String fileName
    ) {
        return PlatformPatterns
            .psiElement()
            .inside(XmlPatterns
                        .xmlAttributeValue()
                        .inside(XmlPatterns
                                    .xmlAttribute()
                                    .withName(attributeName)
                                    .withParent(xmlTag().withName(tag))
                        )
            ).inFile(getXmlFilePattern(fileName));
    }


}
