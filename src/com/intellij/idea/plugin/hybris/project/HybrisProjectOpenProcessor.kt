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
package com.intellij.idea.plugin.hybris.project

import com.intellij.ide.actions.ImportModuleAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisUtil
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectOpenProcessorBase
import com.intellij.util.asSafely

class HybrisProjectOpenProcessor : ProjectOpenProcessorBase<OpenHybrisProjectImportBuilder>() {

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        val jdkTable = ProjectJdkTable.getInstance()
        val orderRootTypes = OrderRootType.getAllTypes()

        runInEdt { jdkTable.preconfigure() }

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

        runInEdt {
            ImportModuleAction.doImport(null) {
                ImportModuleAction.createImportWizard(null, null, virtualFile, *providers)
                    ?.apply {
                        cancelButton.addActionListener {
                            WelcomeFrame.showIfNoProjectOpened()
                        }
                    }
            }
        }

        return null
    }

    override fun canOpenProject(file: VirtualFile) = if (super.canOpenProject(file)) true
    else HybrisUtil.isPotentialHybrisProject(file)

    override val supportedExtensions = arrayOf(
        HybrisConstants.EXTENSION_INFO_XML,
        HybrisConstants.LOCAL_EXTENSIONS_XML,
        HybrisConstants.EXTENSIONS_XML
    )

    override fun doGetBuilder() = ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(OpenHybrisProjectImportBuilder::class.java)

}