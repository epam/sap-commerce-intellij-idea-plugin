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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.impex.assistance.event.ImpexColumnHighlightingCaretListener
import com.intellij.idea.plugin.hybris.impex.assistance.event.ImpexHeaderHighlightingCaretListener
import com.intellij.idea.plugin.hybris.impex.assistance.event.ImpexPsiTreeChangeListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiManager

class ImpexHeaderHighlighterStartupActivity : ProjectActivity, Disposable {

    override suspend fun execute(project: Project) {
        if (!ApplicationManager.getApplication().getService(CommonIdeaService::class.java).isHybrisProject(project)) {
            return
        }

        val disposable = this;

        val eventFactory = EditorFactory.getInstance()

        PsiManager.getInstance(project).addPsiTreeChangeListener(ImpexPsiTreeChangeListener(), disposable)
        eventFactory.eventMulticaster.addCaretListener(ImpexHeaderHighlightingCaretListener(), disposable)
        eventFactory.eventMulticaster.addCaretListener(ImpexColumnHighlightingCaretListener(), disposable)
    }

    override fun dispose() {
    }
}