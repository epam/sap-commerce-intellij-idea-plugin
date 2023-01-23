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
package com.intellij.idea.plugin.hybris.lang.annotation

import com.intellij.idea.plugin.hybris.spring.TSInterceptorSpringBuilderFactory
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

class TSInterceptorGutterAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement !is XmlAttributeValue) return
        if (!TSUtils.isTypeSystemXmlFile(psiElement.getContainingFile())) return
        val parentTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag::class.java)
            ?: return
        val dom = DomManager.getDomManager(parentTag.project).getDomElement(parentTag) as? ItemType
            ?: return

        if (psiElement != dom.code.xmlAttributeValue) return
        if (dom.code.stringValue == null) return

        TSInterceptorSpringBuilderFactory.createGutterBuilder(psiElement.project, dom.code.stringValue!!)
            ?.setAlignment(GutterIconRenderer.Alignment.LEFT)
            ?.createGutterIcon(annotationHolder, psiElement)
    }

}