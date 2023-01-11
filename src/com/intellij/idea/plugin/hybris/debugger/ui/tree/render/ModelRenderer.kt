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
package com.intellij.idea.plugin.hybris.debugger.ui.tree.render

import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.ui.tree.render.ChildrenRenderer
import com.intellij.debugger.ui.tree.render.CompoundRendererProvider
import com.intellij.debugger.ui.tree.render.EnumerationChildrenRenderer
import com.intellij.debugger.ui.tree.render.NodeRendererImpl
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.debugger.engine.ModelFullValueEvaluatorProvider
import com.intellij.openapi.project.Project
import com.sun.jdi.Type
import java.util.concurrent.CompletableFuture
import java.util.function.Function

open class ModelRenderer(
    private val myClassName: String,
    private val project: Project,
) : CompoundRendererProvider() {

    override fun getName() = NodeRendererImpl.DEFAULT_NAME
    override fun getClassName() = myClassName
    override fun isEnabled() = true

    override fun getIsApplicableChecker(): Function<Type?, CompletableFuture<Boolean>> {
        return Function { t ->
            CompletableFuture.completedFuture(
                DebuggerUtils.instanceOf(t, className)
            )
        }
    }

    override fun getChildrenRenderer(): ChildrenRenderer {
        val childrenRenderer = EnumerationChildrenRenderer()
        childrenRenderer.isAppendDefaultChildren = true
        ModelEnumerationChildrenRendererInfoProvider.refreshInfos(childrenRenderer, project, className)
        return childrenRenderer
    }

    override fun getIconRenderer() = ModelValueIconRenderer()
    override fun getFullValueEvaluatorProvider() = ModelFullValueEvaluatorProvider()
    override fun toString() = "ModelRenderer(className='$myClassName')"

    companion object {
        fun createRendererName(className: String) = "${HybrisConstants.DEBUG_MODEL_RENDERER_PREFIX} ${className.substringAfterLast('.')}"
    }
}