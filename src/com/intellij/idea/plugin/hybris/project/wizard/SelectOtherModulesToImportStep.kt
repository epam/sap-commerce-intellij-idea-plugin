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
package com.intellij.idea.plugin.hybris.project.wizard

import com.intellij.ide.JavaUiBundle
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.IdeBorderFactory
import javax.swing.Icon

class SelectOtherModulesToImportStep(context: WizardContext) : AbstractSelectModulesToImportStep(context) {

    override fun updateStep() {
        super.updateStep()
        fileChooser.setBorder(IdeBorderFactory.createTitledBorder(JavaUiBundle.message("project.import.select.title", name), false))

        for (index in 0 until fileChooser.elementCount) {
            val descriptor = fileChooser.getElementAt(index)

            if (descriptor.isPreselected()) {
                fileChooser.setElementMarked(descriptor, true)
            }

            if (descriptor is CCv2ModuleDescriptor || descriptor is AngularModuleDescriptor) {
                descriptor.importStatus = ModuleDescriptorImportStatus.MANDATORY
            }
        }
    }

    override fun getElementIcon(module: ModuleDescriptor): Icon? {
        if (isInConflict(module)) return HybrisIcons.Module.CONFLICT

        return when (module) {
            is MavenModuleDescriptor -> HybrisIcons.Module.MAVEN
            is EclipseModuleDescriptor -> HybrisIcons.Module.ECLIPSE
            is GradleModuleDescriptor -> HybrisIcons.Module.GRADLE
            is AngularModuleDescriptor -> HybrisIcons.Module.ANGULAR
            is CCv2ModuleDescriptor -> HybrisIcons.Module.CCV2
            else -> null
        }
    }

    override fun setList(otherElements: List<ModuleDescriptor>) {
        val allModules = context.hybrisModulesToImport + otherElements
        try {
            this.context.setList(allModules)
        } catch (e: ConfigurationException) {
            // no-op already validated
        }
    }

    override fun getAdditionalFixedElements(): MutableList<ModuleDescriptor> = context.getHybrisModulesToImport()

    @Throws(ConfigurationException::class)
    override fun validate() = validateCommon()

    override fun isStepVisible() = with(context) {
        setExternalStepModuleList()

        list
            ?.isNotEmpty()
            ?: false
    }

    override fun getName() = "Other"
}
