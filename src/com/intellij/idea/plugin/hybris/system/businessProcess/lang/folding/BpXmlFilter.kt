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

package com.intellij.idea.plugin.hybris.system.businessProcess.lang.folding

import com.intellij.idea.plugin.hybris.system.businessProcess.model.Action
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Case
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Process
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Wait
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag

class BpXmlFilter : PsiElementFilter {
    override fun isAccepted(element: PsiElement) = when (element) {
        is XmlTag -> when (element.localName) {
            Action.TRANSITION,
            Process.END,
            Case.CHOICE,
            Wait.CASE,
            Wait.TIMEOUT,
            Wait.EVENT,
            Process.ACTION,
            Process.WAIT -> true

            else -> false
        }

        else -> false
    }
}