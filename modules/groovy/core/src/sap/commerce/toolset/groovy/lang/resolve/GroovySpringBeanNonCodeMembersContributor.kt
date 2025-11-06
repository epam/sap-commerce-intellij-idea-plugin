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
package sap.commerce.toolset.groovy.lang.resolve

import com.intellij.psi.*
import com.intellij.psi.scope.ElementClassHint
import com.intellij.psi.scope.NameHint
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.asSafely
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrImplicitVariableImpl
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.groovy.SpringResolutionMode
import sap.commerce.toolset.spring.SpringHelper

class GroovySpringBeanNonCodeMembersContributor : NonCodeMembersContributor() {

    override fun processDynamicElements(
        qualifierType: PsiType,
        aClass: PsiClass?,
        processor: PsiScopeProcessor,
        place: PsiElement,
        state: ResolveState
    ) {
        qualifierType.asSafely<PsiClassType>() ?: return
        val name = processor.getHint(NameHint.KEY)
            ?.getName(state)
            ?: return
        processor.getHint(ElementClassHint.KEY)
            ?.takeIf { it.shouldProcess(ElementClassHint.DeclarationKind.FIELD) }
            ?: return
        val resolutionMode = place.containingFile.originalFile.virtualFile
            .getUserData(GroovyConstants.KEY_SPRING_RESOLUTION_MODE)
            ?: SpringResolutionMode.DISABLED

        if (resolutionMode == SpringResolutionMode.DISABLED) return

        val resolveBeanClass = SpringHelper.resolveBeanClass(place, name) ?: return
        val fqn = resolveBeanClass.qualifiedName ?: return

        val declaration = GrImplicitVariableImpl(resolveBeanClass.manager, name, fqn, null)

        if (!processor.execute(declaration, state)) return
    }
}
