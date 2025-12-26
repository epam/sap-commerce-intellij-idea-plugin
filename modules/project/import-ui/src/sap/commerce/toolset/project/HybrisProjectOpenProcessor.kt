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
package sap.commerce.toolset.project

import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectOpenProcessorBase
import com.intellij.util.asSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisConstants

class HybrisProjectOpenProcessor : ProjectOpenProcessorBase<HybrisProjectImportBuilder>() {

    private val keyHybrisProjectDirectory = Key.create<Boolean>("IS_HYBRIS_FILE")
    private val skipDirs = setOf(
        ProjectConstants.Directory.NODE_MODULES,

        ProjectConstants.Directory.IDEA,
        ProjectConstants.Directory.SVN,
        ProjectConstants.Directory.GRADLE,

        ProjectConstants.Directory.TEMP,
        ProjectConstants.Directory.TMP,
        ProjectConstants.Directory.LOG,
        ProjectConstants.Directory.DATA,

        ProjectConstants.Directory.CLASSES,
        ProjectConstants.Directory.ECLIPSE_BIN,
        ProjectConstants.Directory.TEST_CLASSES,
        ProjectConstants.Directory.MODEL_CLASSES,

        ProjectConstants.Directory.SRC,
        ProjectConstants.Directory.GEN_SRC,
        ProjectConstants.Directory.GROOVY_SRC,
        ProjectConstants.Directory.KOTLIN_SRC,
        ProjectConstants.Directory.SCALA_SRC,

        ProjectConstants.Directory.TEST_SRC,
        ProjectConstants.Directory.GROOVY_TEST_SRC,
        ProjectConstants.Directory.KOTLIN_TEST_SRC,
        ProjectConstants.Directory.SCALA_TEST_SRC,

        ProjectConstants.Directory.RESOURCES,

        "installer",
        "build-tools",
        "licenses",
        ProjectConstants.Directory.JS_STOREFRONT,
    )

    override suspend fun openProjectAsync(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        val jdkTable = ProjectJdkTable.getInstance()
        val orderRootTypes = OrderRootType.getAllTypes()

        withContext(Dispatchers.EDT) {
            jdkTable.preconfigure()
        }

        ProgressManager.getInstance().runProcessWithProgressSynchronously<Unit, RuntimeException>(
            {
                jdkTable.allJdks.forEach { sdk ->
                    sdk.homeDirectory

                    sdk.sdkType.asSafely<SdkType>()
                        ?.let { sdkType ->
                            orderRootTypes
                                .filter { sdkType.isRootTypeApplicable(it) }
                                .forEach { orderRootType ->
                                    sdk.sdkModificator.getRoots(orderRootType)
                                }
                        }
                }
            },
            "Detecting SDKs...",
            true,
            null
        )

        val providers = ImportModuleAction.getProviders(null).toTypedArray()

        val modules = withContext(Dispatchers.EDT) {
            ImportModuleAction.doImport(null) {
                val wizard = ImportModuleAction.createImportWizard(null, null, virtualFile, *providers)
                    ?.apply {
                        // TODO: also support "ESC"
                        cancelButton.addActionListener {
                            WelcomeFrame.showIfNoProjectOpened()
                        }
                    }
                wizard
            }
        }

        return modules.firstOrNull()
            ?.project
    }

    override fun canOpenProject(file: VirtualFile) = if (super.canOpenProject(file)) true
    else isPotentialHybrisProject(file)

    override val supportedExtensions = arrayOf(
        HybrisConstants.EXTENSION_INFO_XML,
        HybrisConstants.LOCAL_EXTENSIONS_XML,
        HybrisConstants.EXTENSIONS_XML
    )

    override fun doGetBuilder() = ProjectImportBuilder.EXTENSIONS_POINT_NAME
        .findExtensionOrFail(HybrisProjectImportBuilder::class.java)

    private fun isPotentialHybrisProject(root: VirtualFile): Boolean {
        if (root.getUserData(keyHybrisProjectDirectory) == true) return true

        root.putUserData(keyHybrisProjectDirectory, false)

        VfsUtilCore.iterateChildrenRecursively(
            root,
            { !skipDirs.contains(it.name) },
            { fileOrDir: VirtualFile ->
                val hybrisFile = fileOrDir.name == HybrisConstants.LOCAL_EXTENSIONS_XML
                    || fileOrDir.name == HybrisConstants.EXTENSIONS_XML
                    || fileOrDir.name == HybrisConstants.EXTENSION_INFO_XML

                if (hybrisFile) {
                    root.putUserData(keyHybrisProjectDirectory, true)
                }

                !hybrisFile
            }, VirtualFileVisitor.NO_FOLLOW_SYMLINKS, VirtualFileVisitor.limit(6)
        )

        return java.lang.Boolean.TRUE == root.getUserData(keyHybrisProjectDirectory)
    }
}