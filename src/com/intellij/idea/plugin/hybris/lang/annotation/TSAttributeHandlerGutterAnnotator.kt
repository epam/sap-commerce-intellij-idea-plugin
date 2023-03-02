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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.Persistence
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.spring.SpringManager
import com.intellij.spring.model.utils.SpringModelSearchers

class TSAttributeHandlerGutterAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (!canProcess(psiElement)) return

        val attribute = psiElement as XmlAttributeValue
        val project = psiElement.project
        val module = ModuleUtilCore.findModuleForPsiElement(psiElement) ?: return

        val springBeans = SpringManager.getInstance(project).getAllModels(module)
            .mapNotNull { SpringModelSearchers.findBean(it, attribute.value) }
            .map { it.springBean }
            .map { it.xmlTag }

        if (springBeans.isEmpty()) return

        NavigationGutterIconBuilder
            .create(HybrisIcons.SPRING_BEAN)
            .setTargets(springBeans)
            .setTooltipText(
                if (springBeans.size > 1) message("hybris.editor.gutter.spring.bean.definitions")
                else message("hybris.editor.gutter.spring.bean.definition")
            )
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createGutterIcon(annotationHolder, psiElement)
    }

    private fun canProcess(psiElement: PsiElement) = psiElement is XmlAttributeValue
            && psiElement.parent is XmlAttribute
            && (psiElement.parent as XmlAttribute).name == Persistence.ATTRIBUTE_HANDLER
            && TSUtils.isTypeSystemXmlFile(psiElement.getContainingFile())

}
