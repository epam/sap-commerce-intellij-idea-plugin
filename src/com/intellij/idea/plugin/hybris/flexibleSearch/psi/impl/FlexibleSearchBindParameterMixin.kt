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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference.FxSYColumnReference
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.TSResolveResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.io.Serial
import java.util.function.Function
import java.util.function.Predicate

abstract class FlexibleSearchBindParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), FlexibleSearchBindParameter {

    // see -> FlexibleSearchTypes and language bnf for possible Expressions
    override fun getExpression(): FlexibleSearchExpression? = PsiTreeUtil.skipMatching(
        this,
        Function<PsiElement, FlexibleSearchExpression?> {
            it.parent?.asSafely<FlexibleSearchExpression>()
        },
        Predicate<PsiElement> {
            it is FlexibleSearchLiteralExpression
                || it is FlexibleSearchParenExpression
                || it is FlexibleSearchOrExpression
                || it is FlexibleSearchAndExpression
        }
    )
        ?.asSafely<FlexibleSearchExpression>()

    override fun getItemType(): String? = expression
        ?.getChildOfType<FlexibleSearchColumnRefYExpression>()
        ?.getChildOfType<FlexibleSearchYColumnName>()
        ?.reference
        ?.asSafely<FxSYColumnReference>()
        ?.multiResolve(false)
        ?.firstOrNull()
        ?.asSafely<TSResolveResult<DomElement>>()
        ?.meta
        ?.let {
            when (it) {
                is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> it.type
                is TSMetaRelation.TSMetaOrderingAttribute -> it.type
                is TSMetaRelation.TSMetaRelationElement -> it.type
                else -> null
            }
        }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4595969060146424421L
    }
}