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

package com.intellij.idea.plugin.hybris.debugger.engine

import com.intellij.debugger.engine.FullValueEvaluatorProvider
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.CompoundReferenceRenderer
import com.intellij.debugger.ui.tree.render.EnumerationChildrenRenderer
import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.debugger.ui.tree.render.ModelEnumerationChildrenRendererInfoProvider
import com.intellij.idea.plugin.hybris.debugger.ui.tree.render.ModelRenderer
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbService

class ModelFullValueEvaluatorProvider : FullValueEvaluatorProvider {

    override fun getFullValueEvaluator(evaluationContext: EvaluationContextImpl, valueDescriptor: ValueDescriptorImpl) =
        ModelValueEvaluator(evaluationContext, valueDescriptor)

    class ModelValueEvaluator(
        evaluationContext: EvaluationContextImpl,
        private val valueDescriptor: ValueDescriptorImpl
    ) : JavaValue.JavaFullValueEvaluator(
        HybrisI18NBundleUtils.message("hybris.debug.message.node.type.renderer.refresh"),
        evaluationContext
    ) {
        override fun evaluate(callback: XFullValueEvaluationCallback) {
            val value = valueDescriptor.value
            val project = valueDescriptor.project
            val className = value.type().name()
            val rendererName = ModelRenderer.createRendererName(className)

            if (DumbService.isDumb(project)) {
                Notifications.create(
                    NotificationType.INFORMATION,
                    IdeBundle.message("progress.performing.indexing.tasks"),
                    HybrisI18NBundleUtils.message("hybris.notification.debug.dumb.mode.content")
                )
                    .hideAfter(5)
                    .notify(project)
                callback.errorOccurred(HybrisI18NBundleUtils.message("hybris.notification.debug.dumb.mode.content"))
                return
            }

            val renderer = NodeRendererSettings.getInstance().getAllRenderers(project)
                .filterIsInstance<CompoundReferenceRenderer>()
                .find { it.name == rendererName && it.className == className }
                ?: return
            val childrenRenderer = renderer.childrenRenderer

            if (childrenRenderer is EnumerationChildrenRenderer) {
                ModelEnumerationChildrenRendererInfoProvider.refreshInfos(childrenRenderer, project, className, true)
            }

            callback.evaluated("")
        }

        override fun isShowValuePopup() = false
    }
}