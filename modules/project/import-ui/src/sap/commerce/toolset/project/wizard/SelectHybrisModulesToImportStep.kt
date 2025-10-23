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

package sap.commerce.toolset.project.wizard

import com.intellij.ide.util.ElementsChooser
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.table.JBTable
import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.project.settings.ProjectSettings

class SelectHybrisModulesToImportStep(wizard: WizardContext) : AbstractSelectModulesToImportStep(wizard), OpenSupport, RefreshSupport {

    private var selectionMode = ModuleDescriptorImportStatus.MANDATORY

    override fun init() {
        fileChooser.addElementsMarkListener(ElementsChooser.ElementsMarkListener { element, isMarked ->
            if (element is YModuleDescriptor) {
                if (isMarked) {
                    val elementMarkStates = fileChooser.elementMarkStates
                    element.getAllDependencies()
                        .filterNot { BooleanUtils.isNotFalse(elementMarkStates[it]) }
                        .forEach {
                            fileChooser.setElementMarked(it, true)
                            if (selectionMode === ModuleDescriptorImportStatus.MANDATORY) {
                                it.importStatus = ModuleDescriptorImportStatus.MANDATORY
                            }
                        }
                }

                // Re-mark sub-modules accordingly
                markSubmodules(element, isMarked)
            }

            fileChooser.repaint()
        })
    }

    override fun updateStep() {
        context.setCoreStepModuleList()

        super.updateStep()

        selectionMode = ModuleDescriptorImportStatus.MANDATORY

        for (index in 0 until fileChooser.elementCount) {
            val yModuleDescriptor = fileChooser.getElementAt(index)
            if (yModuleDescriptor.isPreselected()) {
                fileChooser.setElementMarked(yModuleDescriptor, true)
                yModuleDescriptor.importStatus = ModuleDescriptorImportStatus.MANDATORY
            }
        }

        selectionMode = ModuleDescriptorImportStatus.UNUSED

        val duplicateModules: MutableSet<String> = HashSet()
        val uniqueModules: MutableSet<String> = HashSet()

        context.list
            ?.forEach {
                if (uniqueModules.contains(it.name)) {
                    duplicateModules.add(it.name)
                } else {
                    uniqueModules.add(it.name)
                }
            }

        // TODO: improve sorting
        fileChooser.sort { o1: ModuleDescriptor, o2: ModuleDescriptor ->
            val o1dup = duplicateModules.contains(o1.name)
            val o2dup = duplicateModules.contains(o2.name)
            if (o1dup xor o2dup) {
                return@sort if (o1dup) -1 else 1
            }
            val o1custom = isCustomDescriptor(o1)
            val o2custom = isCustomDescriptor(o2)
            if (o1custom xor o2custom) {
                return@sort if (o1custom) -1 else 1
            }

            // de-boost mandatory Platform extensions
            val o1ext = isPlatformExtDescriptor(o1)
            val o2ext = isPlatformExtDescriptor(o2)
            if (o1ext xor o2ext) {
                return@sort if (o2ext) -1 else 1
            }
            val o1selected = isMandatoryOrPreselected(o1)
            val o2selected = isMandatoryOrPreselected(o2)
            if (o1selected xor o2selected) {
                return@sort if (o1selected) -1 else 1
            }
            o1.compareTo(o2)
        }
        //scroll to top
        (fileChooser.component as? JBTable)
            ?.changeSelection(0, 0, false, false)
    }

    override fun setList(allElements: MutableList<ModuleDescriptor>) {
        context.hybrisModulesToImport = allElements
    }

    override fun open(projectSettings: ProjectSettings) {
        refresh(projectSettings)
    }

    override fun refresh(projectSettings: ProjectSettings) {
        try {
            val filteredModuleToImport = context.getBestMatchingExtensionsToImport(projectSettings)
            context.list = filteredModuleToImport
        } catch (e: ConfigurationException) {
            // no-op already validated
        }
    }

    override fun isElementEnabled(element: ModuleDescriptor?) = when (element) {
        is PlatformModuleDescriptor -> false
        is YPlatformExtModuleDescriptor -> false
        is ConfigModuleDescriptor if element.isPreselected() && element.isMainConfig -> false
        else -> super.isElementEnabled(element)
    }

    override fun getElementIcon(item: ModuleDescriptor?) = when {
        item == null -> HybrisIcons.Y.LOGO_BLUE

        isInConflict(item) -> HybrisIcons.Extension.CONFLICT

        item is YCustomRegularModuleDescriptor
            || item is ConfigModuleDescriptor
            || item is PlatformModuleDescriptor
            || item is YPlatformExtModuleDescriptor
            || item is YOotbRegularModuleDescriptor -> item.descriptorType.icon

        item is YWebSubModuleDescriptor
            || item is YCommonWebSubModuleDescriptor
            || item is YAcceleratorAddonSubModuleDescriptor
            || item is YBackofficeSubModuleDescriptor
            || item is YHacSubModuleDescriptor
            || item is YHmcSubModuleDescriptor -> item.subModuleDescriptorType.icon

        else -> HybrisIcons.Y.LOGO_BLUE
    }

    private fun isMandatoryOrPreselected(descriptor: ModuleDescriptor) = descriptor.importStatus === ModuleDescriptorImportStatus.MANDATORY
        || descriptor.isPreselected()

    private fun isPlatformExtDescriptor(descriptor: ModuleDescriptor) = descriptor is YPlatformExtModuleDescriptor
        || descriptor is PlatformModuleDescriptor

    private fun isCustomDescriptor(descriptor: ModuleDescriptor) = descriptor is YCustomRegularModuleDescriptor
        || descriptor is ConfigModuleDescriptor
        || (descriptor is YSubModuleDescriptor && descriptor.owner is YCustomRegularModuleDescriptor)

    private fun markSubmodules(yModuleDescriptor: YModuleDescriptor, marked: Boolean) {
        yModuleDescriptor.getSubModules()
            .forEach { fileChooser.setElementMarked(it, marked) }
    }
}