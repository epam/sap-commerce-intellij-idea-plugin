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

class YFacetEditorTab(val state: ExtensionDescriptor) : FacetEditorTab() {

    override fun getDisplayName() = "[y] SAP Commerce Facet"
    override fun isModified() = dialogPanel.isModified()
    override fun createComponent() = dialogPanel

    private val dialogPanel = panel {
        row {
            label("SAP Commerce module descriptor")
                .bold()
        }
        group("Details") {
            row("Name:") {
                label(state.name)
                    .bold()
            }
            row("Type:") {
                icon(
                    when (state.type) {
                        ModuleDescriptorType.CCV2 -> HybrisIcons.EXTENSION_CLOUD
                        ModuleDescriptorType.CUSTOM -> HybrisIcons.EXTENSION_CUSTOM
                        ModuleDescriptorType.OOTB -> HybrisIcons.EXTENSION_OOTB
                        ModuleDescriptorType.EXT -> HybrisIcons.EXTENSION_EXT
                        ModuleDescriptorType.CONFIG -> HybrisIcons.EXTENSION_CONFIG
                        ModuleDescriptorType.PLATFORM -> HybrisIcons.EXTENSION_PLATFORM
                        else -> HybrisIcons.Y_LOGO_BLUE
                    }
                )
                label(state.type.name)
            }
            state.subModuleType
                ?.let {
                    row("Sub-type:") {
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
        }

        if (state.subModuleType == null && state.type != ModuleDescriptorType.PLATFORM && state.type != ModuleDescriptorType.CCV2) {
            group("Settings") {
                row("Read only:") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.readonly)
                }
                row("Deprecated:") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.deprecated)
                }
                row("External dependencies:") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.useMaven)
                }
                    .comment(
                        """Represents <strong>usemaven</strong> flag of the <strong>extensioninfo.xml</strong> file.</br>
                        If enabled, dependencies declared in the <strong>external-dependencies.xml</strong> will be retrieved during the build.
                        """.trimIndent()
                    )
                row("Template extension") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.extGenTemplateExtension)
                }
                row("ModuleGen name") {
                    label(state.moduleGenName ?: "")
                }
            }
            group("Extensibility") {
                row("Core module") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.coreModule)
                }
                    .comment(
                        """
                        Configures a core module for the extension.<br>
                        A core module consists of an items.xml file (and therefore allows to add new types to the system), a manager class, classes for the JaLo Layer and the ServiceLayer and JUnit test classes.<br>
                        The following directories are required: /src, /resources, /testsrc.
                    """.trimIndent()
                    )
                row("Backoffice module") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.backofficeModule)
                }
                    .comment(
                        "If <strong>extensioninfo.xml</strong> has enabled meta <strong>backoffice-module</strong> " +
                            "Backoffice will be available for customization via sub-module."
                    )
                row("Web module") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.webModule)
                }
                    .comment("Configures a web module for the extension. Required directory: <code>/web</code>.")
                row("HAC module") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.hacModule)
                }
                row("HMC module") {
                    checkBox("")
                        .enabled(false)
                        .selected(state.hmcModule)
                }
                    .comment("Configures an hMC module for the extension. Required directory: <code>/hmc</code>.")
            }
        }
    }
}