/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisUtil
import com.intellij.idea.plugin.hybris.project.wizard.OpenSupport
import com.intellij.idea.plugin.hybris.project.wizard.RefreshSupport
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectImportProvider
import com.intellij.projectImport.ProjectOpenProcessorBase

class HybrisProjectOpenProcessor : ProjectOpenProcessorBase<OpenHybrisProjectImportBuilder>() {

    public override fun doQuickImport(file: VirtualFile, wizardContext: WizardContext): Boolean {
        if (file.isDirectory) {
            wizardContext.setProjectFileDirectory(file.path)
        }
        wizardContext.projectName = file.name
        wizardContext.projectBuilder = builder

        builder.refresh = false
        builder.fileToImport = file.path

        val temporarySettings = HybrisProjectSettings()
        hybrisProjectImportProvider()
            ?.createSteps(wizardContext)
            ?.filterIsInstance<OpenSupport>()
            ?.forEach { it.open(temporarySettings) }

        // it has to be set as last property
        builder.isOpenProjectSettingsAfter = true

        return true
    }

    override fun canOpenProject(file: VirtualFile): Boolean {
        val canOpenSimpleVerification = super.canOpenProject(file)
        return if (canOpenSimpleVerification) { true }
        else HybrisUtil.isPotentialHybrisProject(file)
    }

    override val supportedExtensions = arrayOf(
        HybrisConstants.EXTENSION_INFO_XML,
        HybrisConstants.LOCAL_EXTENSIONS_XML,
        HybrisConstants.EXTENSIONS_XML
    )

    override fun doGetBuilder() = ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(OpenHybrisProjectImportBuilder::class.java)

    private fun hybrisProjectImportProvider() = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensions
        .find { it is HybrisProjectImportProvider }

}