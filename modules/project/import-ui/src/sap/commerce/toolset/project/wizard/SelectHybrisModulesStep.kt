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
import com.intellij.util.asSafely
import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.project.ProjectRefreshService
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.project.settings.ySettings

class SelectHybrisModulesStep(context: WizardContext) : AbstractSelectModulesStep(context, ModuleGroup.HYBRIS), RefreshSupport {

    private val orderByType = mapOf(
        ModuleDescriptorType.CONFIG to 0,
        ModuleDescriptorType.CUSTOM to 1,
        ModuleDescriptorType.OOTB to 2,
        ModuleDescriptorType.PLATFORM to 3,
        ModuleDescriptorType.EXT to 4,
    )

    // TODO: restore previous manual selection
    init {
        fileChooser.addElementsMarkListener(ElementsChooser.ElementsMarkListener { element, isMarked ->
            if (element is YModuleDescriptor) {
                if (isMarked) {
                    val elementMarkStates = fileChooser.elementMarkStates
                    element.getAllDependencies()
                        .filterNot { BooleanUtils.isNotFalse(elementMarkStates[it]) }
                        .forEach { fileChooser.setElementMarked(it, true) }
                }

                // Re-mark sub-modules accordingly
                element.getSubModules()
                    .forEach { fileChooser.setElementMarked(it, isMarked) }
            }

            fileChooser.repaint()
        })
    }

    override fun updateStep() {
        val importContext = context.importContext ?: return
        context.list = importContext.foundModules
            .filterNot { it is ExternalModuleDescriptor }
            .sortedWith(
                compareBy<ModuleDescriptor> { orderByType[it.descriptorType] ?: Integer.MAX_VALUE }
                    .thenComparing { !it.isPreselected() }
                    .thenComparing { it.name }
            )

        // init the tree
        super.updateStep()

        context.list
            .filter { it.importStatus == ModuleDescriptorImportStatus.MANDATORY }
            .forEach { fileChooser.disableElement(it) }

        //scroll to top
        fileChooser.component
            ?.asSafely<JBTable>()
            ?.changeSelection(0, 0, false, false)
    }

    override fun refresh(refreshContext: ProjectRefreshContext) {
        val importContext = context.importContext
            ?: return
        val projectSettings = refreshContext.project.ySettings

        try {
            val chosenHybrisModuleDescriptors = buildList {
                val moduleDescriptors = ModuleDescriptorsSelector.getInstance().getSelectableHybrisModules(importContext, projectSettings)
                val openModuleDescriptors = ProjectRefreshService.getInstance(refreshContext.project).openModuleDescriptors(importContext)

                addAll(moduleDescriptors)
                removeAll(openModuleDescriptors)
            }

            importContext.chooseModuleDescriptors(moduleGroup, chosenHybrisModuleDescriptors)
        } catch (_: ConfigurationException) {
            // no-op already validated
        }
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
}
