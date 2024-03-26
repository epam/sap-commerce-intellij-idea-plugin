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
package com.intellij.idea.plugin.hybris.project.providers

import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider

class HybrisWritingAccessProvider(myProject: Project) : WritingAccessProvider() {

    private val ootbReadOnlyMode = ProjectSettingsComponent.getInstance(myProject).state.importOotbModulesInReadOnlyMode

    override fun requestWriting(files: Collection<VirtualFile>) = files
        .filter { isFileReadOnly(it) }
        .toMutableSet()

    override fun isPotentiallyWritable(file: VirtualFile) = !isFileReadOnly(file)

    private fun isFileReadOnly(file: VirtualFile): Boolean {
        if (!ootbReadOnlyMode) return false
        if (!file.isWritable) return true

        return file.path.contains("hybris/bin")
            && !file.path.contains("hybris/bin/custom")
    }
}
