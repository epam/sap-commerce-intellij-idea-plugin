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
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.text

class YFacetEditorTab(val state: YFacetState) : FacetEditorTab() {

    override fun getDisplayName() = "[y] SAP Commerce Facet"
    override fun isModified() = dialogPanel.isModified()

    override fun createComponent() = dialogPanel

    private val dialogPanel = panel {
        group {
            row {
                textField()
                    .enabled(false)
                    .text(state.name)
            }
            row {
                checkBox("Readonly")
                    .enabled(false)
                    .selected(state.readonly)
            }
            row {
                textField()
                    .enabled(false)
                    .text(state.moduleDescriptorType.name)
            }
            state.subModuleDescriptorType
                ?.let {
                    row {
                        textField()
                            .enabled(false)
                            .text(it.name)
                    }
                }
        }
    }
}