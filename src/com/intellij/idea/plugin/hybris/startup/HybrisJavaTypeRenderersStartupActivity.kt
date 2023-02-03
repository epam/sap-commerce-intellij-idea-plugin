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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.tree.render.CompoundReferenceRenderer
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.project.Project
import com.intellij.idea.plugin.hybris.debugger.ui.tree.render.ModelRenderer
import com.intellij.openapi.startup.ProjectPostStartupActivity

/**
 * As for now IDEA does not save icon renderer and full value evaluator with CompoundReferenceRenderer
 * see writeExternal and readExternal methods
 * Due that we have to reset these fields after each start for possible custom [y] renderers
 *
 * related issue report: https://youtrack.jetbrains.com/issue/IDEA-312351
 */
class HybrisJavaTypeRenderersStartupActivity : ProjectPostStartupActivity {

    override suspend fun execute(project: Project) {
        val rendererSettings = NodeRendererSettings.getInstance()
        val oldRenderers = arrayListOf<CompoundReferenceRenderer>()
        val newRenderers = rendererSettings.getAllRenderers(project)
            .filter { rendererSettings.customRenderers.contains(it) }
            .filterIsInstance<CompoundReferenceRenderer>()
            .filter { it.name.startsWith(HybrisConstants.DEBUG_MODEL_RENDERER_PREFIX) && it.className.endsWith(HybrisConstants.MODEL_SUFFIX) }
            .map { oldRenderer ->
                oldRenderers.add(oldRenderer)

                val modelRenderer = object : ModelRenderer(oldRenderer.className, project) {
                    // there is no need to re-evaluate children for preloaded renderers
                    override fun getChildrenRenderer() = oldRenderer.childrenRenderer
                }
                val replacedRenderer = modelRenderer.createRenderer()
                replacedRenderer.name = oldRenderer.name
                return@map replacedRenderer
            }
        oldRenderers.forEach { rendererSettings.customRenderers.removeRenderer(it) }
        newRenderers.forEach { rendererSettings.customRenderers.addRenderer(it) }

        rendererSettings.fireRenderersChanged()
    }
}