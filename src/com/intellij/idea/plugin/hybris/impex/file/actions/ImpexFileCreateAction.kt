/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.file.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.util.isHybrisProject
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile

class ImpexFileCreateAction : CreateFileFromTemplateAction(NEW_FILE, "", HybrisIcons.ImpEx.FILE), DumbAware {

    override fun buildDialog(
        project: Project,
        directory: PsiDirectory,
        builder: CreateFileFromTemplateDialog.Builder
    ) {
        builder.setTitle(NEW_FILE)
            .addKind("Empty file", HybrisIcons.ImpEx.FILE, "ImpEx File")
    }

    override fun postProcess(createdElement: PsiFile, templateName: String, customProperties: Map<String, String>?) {
        super.postProcess(createdElement, templateName, customProperties)
    }

    override fun isAvailable(dataContext: DataContext) = super.isAvailable(dataContext) && dataContext.isHybrisProject
    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String) = NEW_FILE
    override fun getDefaultTemplateProperty() = null
    override fun hashCode() = javaClass.hashCode()
    override fun equals(other: Any?) = other is ImpexFileCreateAction

    companion object {
        private const val NEW_FILE = "ImpEx File"
    }
}
