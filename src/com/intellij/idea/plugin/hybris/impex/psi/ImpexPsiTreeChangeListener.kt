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
package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.assistance.ImpexColumnHighlighterService
import com.intellij.idea.plugin.hybris.impex.assistance.ImpexHeaderNameHighlighterService
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.util.PsiUtilBase

class ImpexPsiTreeChangeListener : PsiTreeChangeListener {

    private val impexColumnHighlighterService = ImpexColumnHighlighterService.instance
    private val impexHeaderNameHighlighterService = ImpexHeaderNameHighlighterService.instance

    private fun highlightHeader(psiTreeChangeEvent: PsiTreeChangeEvent) {
        val file = psiTreeChangeEvent.file ?: return
        val editor = PsiEditorUtil.findEditor(file) ?: return
        val project = editor.project ?: return
        if (project.isDisposed) return

        if (PsiUtilBase.getLanguageInEditor(editor, project) is ImpexLanguage) {
            impexHeaderNameHighlighterService.highlight(editor)
            impexColumnHighlighterService.highlight(editor)
        }
    }

    override fun beforeChildAddition(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun beforeChildRemoval(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun beforeChildReplacement(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun beforeChildMovement(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun beforeChildrenChange(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun beforePropertyChange(psiTreeChangeEvent: PsiTreeChangeEvent) {
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

    override fun childrenChanged(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }

    override fun childMoved(psiTreeChangeEvent: PsiTreeChangeEvent) {
        highlightHeader(psiTreeChangeEvent)
    }

    override fun propertyChanged(psiTreeChangeEvent: PsiTreeChangeEvent) {
    }
}