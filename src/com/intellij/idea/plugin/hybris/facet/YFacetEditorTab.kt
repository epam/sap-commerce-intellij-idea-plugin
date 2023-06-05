/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.facet

import com.intellij.facet.ui.FacetEditorTab
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.SubModuleDescriptorType
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected

class YFacetEditorTab(val state: YFacetState) : FacetEditorTab() {

    override fun getDisplayName() = "[y] SAP Commerce Facet"
    override fun isModified() = dialogPanel.isModified()
    override fun createComponent() = dialogPanel

    private val dialogPanel = panel {
        group("[y] SAP Commerce Module") {
            row("Extension name:") {
                label(state.name)
            }
            row("Descriptor type:") {
                icon(
                    when (state.moduleDescriptorType) {
                        ModuleDescriptorType.CUSTOM -> HybrisIcons.EXTENSION_CUSTOM
                        ModuleDescriptorType.CCV2 -> HybrisIcons.EXTENSION_CLOUD
                        ModuleDescriptorType.OOTB -> HybrisIcons.EXTENSION_OOTB
                        ModuleDescriptorType.EXT -> HybrisIcons.EXTENSION_EXT
                        ModuleDescriptorType.CONFIG -> HybrisIcons.EXTENSION_CONFIG
                        else -> HybrisIcons.HYBRIS
                    }
                )
                label(state.moduleDescriptorType.name)
                    .bold()
            }
            state.subModuleDescriptorType
                ?.let {
                    row("Sub-module type:") {
                        icon(
                            when (it) {
                                SubModuleDescriptorType.HAC -> HybrisIcons.EXTENSION_HAC
                                SubModuleDescriptorType.HMC -> HybrisIcons.EXTENSION_HMC
                                SubModuleDescriptorType.BACKOFFICE -> HybrisIcons.EXTENSION_BACKOFFICE
                                SubModuleDescriptorType.ADDON -> HybrisIcons.EXTENSION_ADDON
                                SubModuleDescriptorType.COMMON_WEB -> HybrisIcons.EXTENSION_COMMON_WEB
                                SubModuleDescriptorType.WEB -> HybrisIcons.EXTENSION_WEB
                            }
                        )
                        label(it.name)
                    }
                }
            row("Read only:") {
                checkBox("")
                    .enabled(false)
                    .selected(state.readonly)
            }
        }
    }
}