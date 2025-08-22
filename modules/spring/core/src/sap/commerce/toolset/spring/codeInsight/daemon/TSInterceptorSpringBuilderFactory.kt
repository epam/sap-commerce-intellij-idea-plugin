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

package sap.commerce.toolset.spring.codeInsight.daemon

import com.intellij.codeInsight.navigation.DomGotoRelatedItem
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.spring.SpringBundle
import com.intellij.spring.gutter.SpringBeansPsiElementCellRenderer
import com.intellij.spring.gutter.groups.SpringGutterIconBuilder
import com.intellij.spring.model.SpringBeanPointer
import com.intellij.spring.model.xml.DomSpringBean
import com.intellij.util.NotNullFunction
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.spring.resolveInterceptorBeansLazy

/**
 * Initial idea taken from SpringBeanAnnotator
 */
object TSInterceptorSpringBuilderFactory {

    private val converter: (dom: SpringBeanPointer<*>) -> Set<PsiElement?> = {
        if (it.isValid) setOf(it.springBean.identifyingPsiElement)
        else emptySet()
    }

    private val gotoRelatedItemProvider = NotNullFunction<SpringBeanPointer<*>, Collection<GotoRelatedItem>> {
        val bean = it.springBean
        return@NotNullFunction if (bean is DomSpringBean) {
            listOf(
                DomGotoRelatedItem(
                    bean,
                    SpringBundle.message("autowired.dependencies.goto.related.item.group.name")
                )
            )
        } else {
            bean.identifyingPsiElement
                ?.let { element ->
                    listOf(
                        GotoRelatedItem(
                            element,
                            SpringBundle.message("autowired.dependencies.goto.related.item.group.name")
                        )
                    )
                }
                ?: emptyList()
        }
    }

    fun createGutterBuilder(project: Project, typeCode: String): SpringGutterIconBuilder<SpringBeanPointer<*>>? {
        val clazz = JavaPsiFacade.getInstance(project)
            .findClass(HybrisConstants.CLASS_FQN_INTERCEPTOR_MAPPING, GlobalSearchScope.allScope(project))
            ?: return null

        val builder = SpringGutterIconBuilder.createBuilder(
            HybrisIcons.TypeSystem.INTERCEPTOR,
            converter,
            gotoRelatedItemProvider
        )
        builder
            .setTargets(clazz.resolveInterceptorBeansLazy(typeCode))
            .setEmptyPopupText(i18n("hybris.editor.gutter.ts.interceptor.no.matches"))
            .setPopupTitle(i18n("hybris.editor.gutter.ts.interceptor.choose.title"))
            .setTooltipText(i18n("hybris.editor.gutter.ts.interceptor.tooltip.text"))
            .setTargetRenderer { SpringBeansPsiElementCellRenderer() }

        return builder
    }

}