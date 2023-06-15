/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.components.PersistentStateComponent

class YFacetConfiguration : FacetConfiguration, PersistentStateComponent<ExtensionDescriptor?> {

    private var extensionDescriptor: ExtensionDescriptor? = null

    override fun createEditorTabs(editorContext: FacetEditorContext, validatorsManager: FacetValidatorsManager) = getState()
        ?.let { arrayOf(YFacetEditorTab(it, editorContext)) }
        ?: emptyArray()

    override fun getState() = extensionDescriptor
    override fun loadState(state: ExtensionDescriptor) {
        this.extensionDescriptor = state
    }
}