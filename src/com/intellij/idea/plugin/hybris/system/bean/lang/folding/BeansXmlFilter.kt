/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.bean.lang.folding

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.model.*
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

class BeansXmlFilter : PsiElementFilter {
    override fun isAccepted(element: PsiElement) = when (element) {
        is XmlTag -> when (element.localName) {
            Bean.PROPERTY,
            Enum.VALUE,
            Beans.BEAN,
            Beans.ENUM,
            AbstractPojo.DESCRIPTION,
            Hints.HINT,
            Bean.HINTS -> true

            else -> false
        }

        is XmlToken -> when (element.text) {
            HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED,
            HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> true

            else -> false
        }

        else -> false
    }
}