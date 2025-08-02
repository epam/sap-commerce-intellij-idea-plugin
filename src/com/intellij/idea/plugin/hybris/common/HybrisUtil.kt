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
package com.intellij.idea.plugin.hybris.common

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.io.File

object HybrisUtil {

    private val SKIP_DIRS = setOf(
        HybrisConstants.NODE_MODULES_DIRECTORY,
        ".idea",
        "classes",
        "temp",
        "log",
        "data",
        "resources",
        "installer",
        "build-tools",
        "src",
        "groovysrc",
        "kotlinsrc",
        "testsrc",
        "groovytestsrc",
        "kotlintestsrc",
        "licenses",
        "js-storefront",
    )

    @JvmStatic
    fun isHybrisModuleRoot(file: File) = File(file, HybrisConstants.EXTENSION_INFO_XML).isFile
    fun isHybrisModuleRoot(file: VirtualFile) = file.findChild(HybrisConstants.EXTENSION_INFO_XML) != null

    fun isPotentialHybrisProject(root: VirtualFile): Boolean {
        if (root.getUserData(KEY_HYBRIS_PROJECT_DIRECTORY) == true) return true

        return ProgressManager.getInstance().runProcessWithProgressSynchronously<Boolean, RuntimeException>(
            {
                ProgressManager.getInstance().progressIndicator.text = "Scanning for SAP Commerce project structure..."
                root.putUserData(KEY_HYBRIS_PROJECT_DIRECTORY, false)

                VfsUtilCore.iterateChildrenRecursively(
                    root,
                    { !SKIP_DIRS.contains(it.name) },
                    { fileOrDir: VirtualFile ->
                        val hybrisFile = fileOrDir.name == HybrisConstants.LOCAL_EXTENSIONS_XML
                            || fileOrDir.name == HybrisConstants.EXTENSIONS_XML
                            || fileOrDir.name == HybrisConstants.EXTENSION_INFO_XML

                        if (hybrisFile) {
                            root.putUserData(KEY_HYBRIS_PROJECT_DIRECTORY, true)
                        }

                        !hybrisFile
                    }, VirtualFileVisitor.NO_FOLLOW_SYMLINKS, VirtualFileVisitor.limit(6)
                )

                java.lang.Boolean.TRUE == root.getUserData(KEY_HYBRIS_PROJECT_DIRECTORY)
            },
            "Detecting SAP Commerce Project",
            true,
            null  // project, null is OK outside project context
        )
    }

    private val KEY_HYBRIS_PROJECT_DIRECTORY = Key.create<Boolean>("IS_HYBRIS_FILE")
}
