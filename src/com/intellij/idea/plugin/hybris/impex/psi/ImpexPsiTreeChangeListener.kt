/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.assistance.ImpexColumnHighlighterService
import com.intellij.idea.plugin.hybris.impex.assistance.ImpexHeaderNameHighlighterService
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.util.PsiUtilBase

class ImpexPsiTreeChangeListener : PsiTreeChangeListener {

    private fun highlightHeader(psiTreeChangeEvent: PsiTreeChangeEvent) {
        val file = psiTreeChangeEvent.file ?: return
        val editor = PsiEditorUtil.findEditor(file) ?: return
        val project = editor.project ?: return
        if (project.isDisposed) return

        if (PsiUtilBase.getLanguageInEditor(editor, project) is ImpexLanguage) {
            ImpexHeaderNameHighlighterService.instance.highlight(editor)
            ImpexColumnHighlighterService.instance.highlight(editor)
        }
    }

    override fun childAdded(psiTreeChangeEvent: PsiTreeChangeEvent) {
        highlightHeader(psiTreeChangeEvent)
    }

    override fun childRemoved(psiTreeChangeEvent: PsiTreeChangeEvent) {
        highlightHeader(psiTreeChangeEvent)
    }

    override fun childReplaced(psiTreeChangeEvent: PsiTreeChangeEvent) {
        highlightHeader(psiTreeChangeEvent)
    }

    override fun childMoved(psiTreeChangeEvent: PsiTreeChangeEvent) {
        highlightHeader(psiTreeChangeEvent)
    }


    override fun beforeChildAddition(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun beforeChildRemoval(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun beforeChildReplacement(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun beforeChildMovement(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun beforeChildrenChange(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun beforePropertyChange(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun childrenChanged(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
    override fun propertyChanged(psiTreeChangeEvent: PsiTreeChangeEvent) = Unit
}