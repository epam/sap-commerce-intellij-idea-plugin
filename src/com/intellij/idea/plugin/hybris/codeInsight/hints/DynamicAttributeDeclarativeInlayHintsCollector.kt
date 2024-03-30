/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.codeInsight.hints

import com.intellij.codeInsight.completion.CompletionMemory
import com.intellij.codeInsight.completion.JavaMethodCallElement
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.PersistenceType
import com.intellij.idea.plugin.hybris.system.type.spring.TSSpringHelper
import com.intellij.idea.plugin.hybris.system.type.util.TSUtils
import com.intellij.psi.*

class DynamicAttributeDeclarativeInlayHintsCollector : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (!element.isValid || element.project.isDefault) return
        if (element !is PsiMethodCallExpression) return

        val method = (if (JavaMethodCallElement.isCompletionMode(element)) CompletionMemory.getChosenMethod(element) else null)
            ?: element.resolveMethodGenerics().element
            ?: return

        if (method !is PsiMethod) return
        val psiClass = method.containingClass ?: return
        if (!TSUtils.isItemModelFile(psiClass)) return

        val meta = TSMetaModelAccess.getInstance(element.project).findMetaItemByName(cleanSearchName(psiClass?.name)) ?: return
        val annotation = method.getAnnotation(HybrisConstants.CLASS_FQN_ANNOTATION_ACCESSOR) ?: return

        val qualifier = annotation.parameterList.attributes
            .filter { it.name == Attribute.QUALIFIER }
            .map { it.value }
            .filterIsInstance<PsiLiteralExpression>()
            .map { it.value }
            .firstOrNull { it != null } ?: return

        val attribute = meta.allAttributes[qualifier]
            ?.takeIf { it.persistence.type == PersistenceType.DYNAMIC }
            ?: return

        val identifier = element.methodExpression.lastChild

        val inlayActionData = attribute.persistence
            .attributeHandler
            ?.let { TSSpringHelper.resolveBeanClass(element, it) }
            ?.let { SmartPointerManager.getInstance(element.project).createSmartPsiElementPointer(it) }
            ?.let {
                InlayActionData(
                    PsiPointerInlayActionPayload(it),
                    DynamicAttributeDeclarativeInlayActionHandler.ID,
                )
            }

        sink.addPresentation(
            InlineInlayPosition(identifier.textRange.startOffset, true),
            tooltip = "Navigate to the ${meta.name}.${attribute.name} dynamic attribute handler",
            hasBackground = true
        ) {
            text(message("hybris.ts.type.dynamic") + (inlayActionData?.let { "⌝" } ?: ""), inlayActionData)
        }
    }

    private fun cleanSearchName(searchName: String?): String? {
        if (searchName == null) {
            return null
        }
        val idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX)
        return if (idx == -1) {
            searchName
        } else searchName.substring(0, idx)
    }

}