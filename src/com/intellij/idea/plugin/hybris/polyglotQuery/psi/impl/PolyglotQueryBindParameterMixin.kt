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

package com.intellij.idea.plugin.hybris.polyglotQuery.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryBindParameter
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryExprAtom
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.reference.PolyglotQueryAttributeKeyNameReference
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.TSResolveResult
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import java.io.Serial

abstract class PolyglotQueryBindParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), PolyglotQueryBindParameter {

    override fun getOperator() = parentOfType<PolyglotQueryExprAtom>()
        ?.cmpOperator

    override fun getItemType(): String? = PsiTreeUtil.getParentOfType(this, PolyglotQueryExprAtom::class.java)
        ?.attributeKey
        ?.attributeKeyName
        ?.reference
        ?.asSafely<PolyglotQueryAttributeKeyNameReference>()
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

    override fun getValue() = text.removePrefix("?")

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6098756117259395199L
    }
}