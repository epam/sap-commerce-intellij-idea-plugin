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

package sap.commerce.toolset.project.tasks

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.vfs.VirtualFileSystemService
import java.io.File

@Service
class PlatformDirectoryLookupTask {

    fun find(rootProjectDirectory: File) = runWithModalProgressBlocking(
        owner = ModalTaskOwner.guess(),
        title = i18n("hybris.project.import.searching.hybris.distribution"),
    ) {
        VirtualFileSystemService.getInstance().findFileByNameRecursively(
            rootProjectDirectory,
            HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME,
            this@runWithModalProgressBlocking,
            ProjectImportConstants.excludedFromScanningDirectories
        )
            ?.parentFile
            ?.parentFile
            ?.parentFile
            ?.absolutePath
            ?: return@runWithModalProgressBlocking null
    }

    companion object {
        fun getInstance(): PlatformDirectoryLookupTask = application.service()
    }
}