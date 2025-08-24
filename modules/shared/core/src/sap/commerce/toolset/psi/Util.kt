/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.psi

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PsiNavigateUtil
import com.intellij.util.asSafely

/**
 * <tagName attributeName="XmlAttributeValue"/>
 */
fun tagAttributeValuePattern(
    rootTag: String,
    tagName: String,
    attributeName: String
): XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue()
    .withAncestor(6, XmlPatterns.xmlTag().withLocalName(rootTag))
    .withParent(
        XmlPatterns.xmlAttribute(attributeName)
            .withParent(XmlPatterns.xmlTag().withName(tagName))
    )
    .inside(insideTagPattern(tagName))

/**
 * <tagName attributeName="XmlAttributeValue"/>
 */
fun tagAttributeValuePattern(
    rootTag: String,
    attributeName: String,
) = XmlPatterns.xmlAttributeValue()
    .withAncestor(6, XmlPatterns.xmlTag().withLocalName(rootTag))
    .and(
        XmlPatterns.xmlAttributeValue()
            .withParent(
                XmlPatterns.xmlAttribute(attributeName)
                    .withParent(XmlPatterns.xmlTag())
            )
            .inside(PlatformPatterns.psiElement(XmlTag::class.java))
    )

fun insideTagPattern(insideTagName: String) = PlatformPatterns.psiElement(XmlTag::class.java)
    .withName(insideTagName)

fun tagAttributePattern(
    tag: String,
    attributeName: String,
    fileName: String?
) = PlatformPatterns.psiElement().inside(
    XmlPatterns.xmlAttributeValue()
        .inside(
            XmlPatterns.xmlAttribute()
                .withName(attributeName)
                .withParent(XmlPatterns.xmlTag().withName(tag))
        )
)
    .inFile(getXmlFilePattern(fileName))

fun navigate(psiElement: PsiElement?, requestFocus: Boolean = true) = PsiNavigateUtil
    .navigate(psiElement, requestFocus)

fun navigate(descriptor: ProblemDescriptor, psiElement: PsiElement?, requestFocus: Boolean = true) = descriptor.asSafely<ProblemDescriptorBase>()
    ?.let { navigate(psiElement, requestFocus) }

fun shouldCreateNewReference(reference: PsiReference?, text: String?) = when {
    reference == null -> true
    else -> reference.asSafely<PsiReferenceBase<*>>()
        ?.let { text != null && (text.length != it.getRangeInElement().length || text != it.getValue()) }
        ?: false
}

fun getValidResults(resolveResults: Array<out ResolveResult>) = resolveResults
    .filter { it.isValidResult }
    .toTypedArray()

private fun getXmlFilePattern(fileName: String?) = if (fileName == null) anyXmlFilePattern else PlatformPatterns.psiFile()
    .withName(StandardPatterns.string().equalTo(fileName))

private val anyXmlFilePattern = PlatformPatterns.psiFile()
    .withName(StandardPatterns.string().endsWith(".xml"))
