/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.flexibleSearch.file.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.idea.plugin.hybris.actions.ActionUtils.isHybrisContext
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class FlexibleSearchFileCreateAction : CreateFileFromTemplateAction(NEW_FS_FILE, "", HybrisIcons.FXS_FILE), DumbAware {

    override fun buildDialog(
        project: Project,
        directory: PsiDirectory,
        builder: CreateFileFromTemplateDialog.Builder
    ) {
        builder.setTitle(NEW_FS_FILE)
            .addKind("Empty file", HybrisIcons.FXS_FILE, FILE_TEMPLATE)
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String) = NEW_FS_FILE

    override fun hashCode() = javaClass.hashCode()

    override fun equals(other: Any?) = other is FlexibleSearchFileCreateAction

    override fun isAvailable(dataContext: DataContext) = super.isAvailable(dataContext)
        && isHybrisContext(dataContext)

    companion object {
        const val FILE_TEMPLATE = "FlexibleSearch File"
        private const val NEW_FS_FILE = "FlexibleSearch File"
    }
}
