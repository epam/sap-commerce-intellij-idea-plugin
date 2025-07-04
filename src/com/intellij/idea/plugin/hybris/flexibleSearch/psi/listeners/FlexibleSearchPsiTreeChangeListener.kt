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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.listeners

import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.extensions.ExtensionNotApplicableException
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.util.asSafely

class FlexibleSearchPsiTreeChangeListener(project: Project) : PsiTreeChangeListener {

    init {
        if (!ProjectSettingsComponent.getInstance(project).isHybrisProject()) throw ExtensionNotApplicableException.create()
    }

    override fun beforeChildAddition(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildRemoval(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildReplacement(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildMovement(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildrenChange(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforePropertyChange(event: PsiTreeChangeEvent) = doChange(event)
    override fun childAdded(event: PsiTreeChangeEvent) = doChange(event)
    override fun childRemoved(event: PsiTreeChangeEvent) = doChange(event)
    override fun childReplaced(event: PsiTreeChangeEvent) = doChange(event)
    override fun childrenChanged(event: PsiTreeChangeEvent) = doChange(event)
    override fun childMoved(event: PsiTreeChangeEvent) = doChange(event)
    override fun propertyChanged(event: PsiTreeChangeEvent) = doChange(event)

    private fun doChange(event: PsiTreeChangeEvent) = event.file
        ?.asSafely<FlexibleSearchFile>()
        ?.let { FileEditorManager.getInstance(it.project).getSelectedEditor(it.virtualFile) }
        ?.asSafely<FlexibleSearchSplitEditor>()
        ?.refreshParameters()
        ?: Unit
}