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
package com.intellij.idea.plugin.hybris.beans.annotators

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.beans.BeansUtils
import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModelAccess
import com.intellij.idea.plugin.hybris.beans.model.Enum
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

class BeansEnumGutterAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (!canProcess(psiElement)) return

        val parentTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag::class.java)
            ?: return

        val project = parentTag.project
        val dom = DomManager.getDomManager(project).getDomElement(parentTag) as? Enum
            ?: return

        if (psiElement != dom.clazz.xmlAttributeValue) return

        val alternativeDoms = findAlternativeDoms(dom, project)

        if (alternativeDoms.isNotEmpty()) {
            NavigationGutterIconBuilder
                .create(AllIcons.Actions.Forward) { _: Any? -> alternativeDoms }
                .setTarget(dom)
                .setTooltipText(
                    if (alternativeDoms.size > 1)
                        HybrisI18NBundleUtils.message("hybris.editor.gutter.alternativeDefinitions")
                    else HybrisI18NBundleUtils.message(
                        "hybris.editor.gutter.alternativeDefinition"
                    )
                )
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createGutterIcon(annotationHolder, psiElement)
        }
    }

    private fun canProcess(psiElement: PsiElement) = psiElement is XmlAttributeValue
            && BeansUtils.isBeansXmlFile(psiElement.getContainingFile())

    private fun findAlternativeDoms(sourceDom: Enum, project: Project) = BeansMetaModelAccess.getInstance(project).findMetaForDom(sourceDom)?.retrieveAllDoms()
            ?.filter { dom -> dom != sourceDom }
            ?.map { it.clazz }
            ?.sortedBy { it.module?.name }
            ?.mapNotNull { it.xmlAttributeValue }
            ?: emptyList();

}