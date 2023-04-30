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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.impex.psi.ImpexPsiNamedElement
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.io.Serial

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
open class ImpexPsiNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ImpexPsiNamedElement {

    override fun setName(newName: String): PsiElement = com.intellij.idea.plugin.hybris.impex.psi.util.setName(this, newName)

    override fun getName(): String? {
        return getKey()
    }

    fun getKey(): String? {
        val keyNode = this.node.findChildByType(ImpexTypes.VALUE)
        if (keyNode != null) {
            // IMPORTANT: Convert embedded escaped spaces to simple spaces
            return keyNode.text.replace("\\\\ ", " ")
        } else {
            return null
        }
    }

    override fun toString() = text ?: super.toString()

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3034402626341816420L
    }
}