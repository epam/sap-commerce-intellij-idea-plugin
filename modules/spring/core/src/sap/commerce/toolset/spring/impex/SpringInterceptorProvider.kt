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
package sap.commerce.toolset.spring.impex

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.impex.constants.modifier.InterceptorProvider
import sap.commerce.toolset.spring.psi.reference.SpringReference
import sap.commerce.toolset.spring.resolveInterceptorBeansLazy

class SpringInterceptorProvider : InterceptorProvider {

    override fun collect(project: Project, parentClassFqn: String): Collection<InterceptorProvider.Bean> {
        if (Plugin.SPRING.isDisabled()) return emptyList()

        val interceptorClass = JavaPsiFacade.getInstance(project)
            .findClass(HybrisConstants.CLASS_FQN_INTERCEPTOR_MAPPING, GlobalSearchScope.allScope(project))
            ?: return emptySet()

        return interceptorClass.resolveInterceptorBeansLazy().value
            .mapNotNull {
                val beanName = it.springBean.beanName ?: return@mapNotNull null
                val className = it.beanClass?.name ?: "?"
                InterceptorProvider.Bean(beanName, className)
            }
            .toSet()
    }

    override fun reference(element: PsiElement, name: String) = SpringReference(element, name)
}
