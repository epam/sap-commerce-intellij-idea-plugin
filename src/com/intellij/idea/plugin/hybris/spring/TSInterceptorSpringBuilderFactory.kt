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

package com.intellij.idea.plugin.hybris.spring

import com.intellij.codeInsight.navigation.DomGotoRelatedItem
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.spring.SpringBundle
import com.intellij.spring.gutter.SpringBeansPsiElementCellRenderer
import com.intellij.spring.gutter.groups.SpringGutterIconBuilder
import com.intellij.spring.java.SpringJavaClassInfo
import com.intellij.spring.model.SpringBeanPointer
import com.intellij.spring.model.xml.DomSpringBean
import com.intellij.spring.model.xml.beans.SpringBean
import com.intellij.util.NotNullFunction

/**
 * Initial idea taken from SpringBeanAnnotator
 */
object TSInterceptorSpringBuilderFactory {

    fun createGutterBuilder(project: Project, typeCode : String): SpringGutterIconBuilder<SpringBeanPointer<*>>? {
        val clazz = JavaPsiFacade.getInstance(project)
            .findClass("de.hybris.platform.servicelayer.interceptor.impl.InterceptorMapping", GlobalSearchScope.allScope(project))
            ?: return null

        val builder = SpringGutterIconBuilder.createBuilder(
            HybrisIcons.INTERCEPTOR,
            { pointer: SpringBeanPointer<*> ->
                if (!pointer.isValid) emptySet() else setOf(
                    pointer.springBean.identifyingPsiElement
                )
            },
            NotNullFunction<SpringBeanPointer<*>, Collection<GotoRelatedItem>> { pointer: SpringBeanPointer<*> ->
                val bean = pointer.springBean
                if (bean is DomSpringBean) {
                    return@NotNullFunction listOf<DomGotoRelatedItem>(
                        DomGotoRelatedItem(
                            bean,
                            SpringBundle.message("autowired.dependencies.goto.related.item.group.name", *arrayOfNulls<Any>(0))
                        )
                    )
                } else {
                    val element = bean.identifyingPsiElement
                    return@NotNullFunction if (element != null) listOf<GotoRelatedItem>(
                        GotoRelatedItem(
                            element,
                            SpringBundle.message("autowired.dependencies.goto.related.item.group.name", *arrayOfNulls<Any>(0))
                        )
                    ) else emptyList<GotoRelatedItem>()
                }
            }
        )
        builder
            .setTargets(getBeansLazy(clazz, typeCode))
            .setEmptyPopupText(message("hybris.editor.gutter.ts.interceptor.no.matches"))
            .setPopupTitle(message("hybris.editor.gutter.ts.interceptor.choose.title"))
            .setTooltipText(message("hybris.editor.gutter.ts.interceptor.tooltip.text"))
            .setCellRenderer { SpringBeansPsiElementCellRenderer() }

        return builder
    }


    private fun getBeansLazy(
        clazz: PsiClass,
        name: String?
    ): NotNullLazyValue<MutableCollection<out SpringBeanPointer<*>>> = NotNullLazyValue.lazy {
        SpringJavaClassInfo.getSpringJavaClassInfo(clazz).resolve().mappedDomBeans
            .asSequence()
            .map { it.springBean }
            .filterIsInstance<SpringBean>()
            .filter { bean ->
                bean.properties.find { property ->
                    property.propertyName == "typeCode" && name.equals(property.valueAsString, true)
                } != null
            }
            .mapNotNull { bean -> bean.properties.find { property -> property.propertyName == "interceptor" } }
            .mapNotNull { interceptor -> interceptor.refValue }
            .sortedWith(SpringBeanPointer.DISPLAY_COMPARATOR)
            .toMutableList()
    }
}