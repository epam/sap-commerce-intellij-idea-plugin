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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.spring.TSInterceptorSpringBuilderFactory
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import java.util.*

class TSInterceptorLineMarkerProvider : AbstractItemLineMarkerProvider<PsiClass>() {

    override fun canProcess(psi: PsiElement?) = psi is PsiClass
    override fun getEmptyPopupText() = message("hybris.editor.gutter.ts.interceptor.no.matches")
    override fun getPopupTitle() = message("hybris.editor.gutter.ts.interceptor.choose.title")
    override fun getTooltipText() = message("hybris.editor.gutter.ts.interceptor.tooltip.text")

    override fun collectDeclarations(psi: PsiClass?): Optional<RelatedItemLineMarkerInfo<PsiElement>> {
        if (psi == null) return Optional.empty()
        if (psi.nameIdentifier == null) return Optional.empty()

        val name = cleanSearchName(psi.name)
        val project = psi.project

        return TSInterceptorSpringBuilderFactory.createGutterBuilder(project, name)
            ?.createSpringGroupLineMarkerInfo(psi.nameIdentifier!!)
            ?.let { Optional.of(it) }
            ?: Optional.empty()
    }

}