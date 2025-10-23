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

package sap.commerce.toolset.project

import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectImportProvider
import com.intellij.projectImport.ProjectOpenProcessor
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.wizard.*

class HybrisProjectImportProvider : ProjectImportProvider() {

    override fun getFileSample() = i18n("hybris.project.import.dialog.message")

    override fun canImportFromFile(file: VirtualFile): Boolean = ProjectOpenProcessor.EXTENSION_POINT_NAME
        .findExtensionOrFail(HybrisProjectOpenProcessor::class.java)
        .canOpenProject(file)

    override fun doGetBuilder(): DefaultHybrisProjectImportBuilder = ProjectImportBuilder.EXTENSIONS_POINT_NAME
        .findExtensionOrFail(DefaultHybrisProjectImportBuilder::class.java)

    override fun createSteps(context: WizardContext) = arrayOf(
        CheckRequiredPluginsWizardStep(context),
        InformationStep(context),
        ProjectImportWizardRootStep(context),
        SelectHybrisModulesToImportStep(context),
        SelectOtherModulesToImportStep(context),
        ProjectWizardStepFactory.getInstance().createProjectJdkStep(context),
    )
}