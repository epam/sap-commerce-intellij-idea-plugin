/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.project.wizard

import com.intellij.ide.util.ElementsChooser
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.table.JBTable
import org.apache.commons.lang3.BooleanUtils

class SelectHybrisModulesToImportStep(wizard: WizardContext) : AbstractSelectModulesToImportStep(wizard), OpenSupport, RefreshSupport {

    private var selectionMode = ModuleDescriptorImportStatus.MANDATORY

    override fun init() {
        fileChooser.addElementsMarkListener(ElementsChooser.ElementsMarkListener<ModuleDescriptor> { element, isMarked ->
            if (element is YModuleDescriptor) {
                if (isMarked) {
                    element.getAllDependencies()
                        .filterNot { BooleanUtils.isNotFalse(fileChooser.elementMarkStates[it]) }
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

    override fun setList(allElements: MutableList<ModuleDescriptor>?) {
        context.setHybrisModulesToImport(allElements)
    }

    override fun open(settings: HybrisProjectSettings?) {
        refresh(settings)
    }

    override fun refresh(settings: HybrisProjectSettings?) {
        try {
            val filteredModuleToImport = context.getBestMatchingExtensionsToImport(settings)
            context.setList(filteredModuleToImport)
        } catch (e: ConfigurationException) {
            // no-op already validated
        }
    }

    override fun isElementEnabled(element: ModuleDescriptor?) = when {
        element is PlatformModuleDescriptor -> false
        element is YPlatformExtModuleDescriptor -> false
        element is ConfigModuleDescriptor && element.isPreselected() -> false
        else -> super.isElementEnabled(element)
    }

    override fun getElementIcon(item: ModuleDescriptor?) = when {
        item == null -> HybrisIcons.Y_LOGO_BLUE
        isInConflict(item) -> HybrisIcons.CANCEL
        item is YCustomRegularModuleDescriptor -> HybrisIcons.EXTENSION_CUSTOM
        item is ConfigModuleDescriptor -> HybrisIcons.EXTENSION_CONFIG
        item is PlatformModuleDescriptor -> HybrisIcons.EXTENSION_PLATFORM
        item is YPlatformExtModuleDescriptor -> HybrisIcons.EXTENSION_EXT
        item is YOotbRegularModuleDescriptor -> HybrisIcons.EXTENSION_OOTB
        item is YWebSubModuleDescriptor -> HybrisIcons.EXTENSION_WEB
        item is YCommonWebSubModuleDescriptor -> HybrisIcons.EXTENSION_COMMON_WEB
        item is YAcceleratorAddonSubModuleDescriptor -> HybrisIcons.EXTENSION_ADDON
        item is YBackofficeSubModuleDescriptor -> HybrisIcons.EXTENSION_BACKOFFICE
        item is YHacSubModuleDescriptor -> HybrisIcons.EXTENSION_HAC
        item is YHmcSubModuleDescriptor -> HybrisIcons.EXTENSION_HMC
        else -> HybrisIcons.Y_LOGO_BLUE
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