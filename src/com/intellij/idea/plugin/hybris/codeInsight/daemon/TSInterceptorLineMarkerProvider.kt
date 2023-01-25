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
package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.spring.TSInterceptorSpringBuilderFactory
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import java.util.*

class TSInterceptorLineMarkerProvider : AbstractItemLineMarkerProvider<PsiField>() {

    override fun canProcess(psi: PsiElement?) = psi is PsiField
            && psi.name == HybrisConstants.TYPECODE_FIELD_NAME

    override fun getEmptyPopupText() = message("hybris.editor.gutter.ts.interceptor.no.matches")
    override fun getPopupTitle() = message("hybris.editor.gutter.ts.interceptor.choose.title")
    override fun getTooltipText() = message("hybris.editor.gutter.ts.interceptor.tooltip.text")

    override fun collectDeclarations(psi: PsiField?): Optional<RelatedItemLineMarkerInfo<PsiElement>> {
        val psiClass = psi?.parentOfType<PsiClass>() ?: return Optional.empty()
        if (!ModelsUtils.isModelFile(psiClass)) return Optional.empty()
        val project = psi.project

        return psi.childrenOfType<PsiLiteralExpression>()
            .mapNotNull {
                val typeCode = it.value.toString()
                TSInterceptorSpringBuilderFactory.createGutterBuilder(project, typeCode)
                    ?.createSpringGroupLineMarkerInfo(psi.nameIdentifier)
            }
            .map { Optional.of(it) }
            .firstOrNull()
            ?: Optional.empty()
    }

}