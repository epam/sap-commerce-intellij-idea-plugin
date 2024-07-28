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

package com.intellij.idea.plugin.hybris.system.cockpitng.util.xml

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.java.completion.JavaClassCompletionService
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.ResolvingConverter

class CngMoldClassConverter : ResolvingConverter<PsiClass>() {

    override fun toString(t: PsiClass?, context: ConvertContext) = t?.qualifiedName

    override fun fromString(s: String?, context: ConvertContext) = s
        ?.let {
            JavaPsiFacade.getInstance(context.project)
                .findClass(s, GlobalSearchScope.allScope(context.project))
        }
        ?.takeIf { psiClass ->
            PsiClassImplUtil.getAllSuperClassesRecursively(psiClass)
                .any { HybrisConstants.CLASS_FQN_CNG_COLLECTION_BROWSER_MOLD_STRATEGY == it.qualifiedName }
        }

    override fun getVariants(context: ConvertContext) = JavaClassCompletionService.getInstance(context.project)
        .getImplementationsPsiClassesForClasses(HybrisConstants.CLASS_FQN_CNG_COLLECTION_BROWSER_MOLD_STRATEGY)
        .distinctBy { it.qualifiedName }

    override fun createLookupElement(t: PsiClass?): LookupElement? {
        val project = t?.project ?: return null

        return JavaClassCompletionService.getInstance(project)
            .createLookupElement(t)
    }

}