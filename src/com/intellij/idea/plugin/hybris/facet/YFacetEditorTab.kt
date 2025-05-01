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

package com.intellij.idea.plugin.hybris.facet

import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.SubModuleDescriptorType
import com.intellij.openapi.module.ModuleManager
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.text

class YFacetEditorTab(
    val state: ExtensionDescriptor,
    private val editorContext: FacetEditorContext
) : FacetEditorTab() {

    override fun getDisplayName() = "[y] SAP Commerce Facet"
    override fun isModified() = dialogPanel.isModified()
    override fun createComponent() = dialogPanel

    private val dialogPanel = panel {
        row {
            label("SAP Commerce module descriptor")
                .bold()
        }
        group("Details") {
            row {
                label("Name:")
                label(state.name)
                    .bold()
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Type:")
                label(state.type.name)
                icon(state.type.icon)
            }.layout(RowLayout.PARENT_GRID)

            state.subModuleType
                ?.let {
                    row {
                        label("Sub-type:")
                        label(it.name)
                        icon(it.icon)
                    }.layout(RowLayout.PARENT_GRID)
                }
        }

        if (state.subModuleType == SubModuleDescriptorType.ADDON && state.installedIntoExtensions.isNotEmpty()) {
            group("Installed Into Extensions") {
                ModuleManager.getInstance(editorContext.project)
                    .modules
                    .mapNotNull { YFacet.getState(it) }
                    .filter { state.installedIntoExtensions.contains(it.name) }
                    .map {
                        row {
                            icon(it.type.icon)
                            label(it.name)
                                .bold()
                        }
                    }
            }
        }

        if (state.subModuleType == null
            && state.type != ModuleDescriptorType.PLATFORM
            && state.type != ModuleDescriptorType.CCV2
            && state.type != ModuleDescriptorType.ANGULAR
        ) {
            group("Settings") {
                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.readonly)
                    label("Read only")
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.deprecated)
                    label("Deprecated")
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.useMaven)
                    label("External dependencies")
                        .comment(
                            """Represents <strong>usemaven</strong> flag of the <strong>extensioninfo.xml</strong> file.</br>
                                If enabled, dependencies declared in the <strong>external-dependencies.xml</strong> will be retrieved during the build.
                        """.trimIndent()
                        )
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.extGenTemplateExtension)
                    label("Template extension")
                }.layout(RowLayout.PARENT_GRID)

                row {
                    label("ModuleGen name:")
                    textField()
                        .text(state.moduleGenName ?: "")
                        .enabled(false)
                }
            }
            group("Extensibility") {
                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.coreModule)
                    label("Core module")
                        .comment(
                            """
                        Configures a core module for the extension.<br>
                        A core module consists of an items.xml file (and therefore allows to add new types to the system), a manager class, classes for the JaLo Layer and the ServiceLayer and JUnit test classes.<br>
                        The following directories are required: /src, /resources, /testsrc.
                    """.trimIndent()
                        )
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.backofficeModule)
                    label("Backoffice module")
                        .comment(
                            "If <strong>extensioninfo.xml</strong> has enabled meta <strong>backoffice-module</strong> " +
                                "Backoffice will be available for customization via sub-module."
                        )
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.webModule)
                    label("Web module")
                        .comment("Configures a web module for the extension. Required directory: <code>/web</code>.")
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.hacModule)
                    label("HAC module")
                }.layout(RowLayout.PARENT_GRID)

                row {
                    checkBox("")
                        .enabled(false)
                        .selected(state.hmcModule)
                    label("HMC module")
                        .comment("Configures an hMC module for the extension. Required directory: <code>/hmc</code>.")
                }.layout(RowLayout.PARENT_GRID)
            }
        }
    }
}