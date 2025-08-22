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
package sap.commerce.toolset.debugger.ui.tree.render

import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.FullValueEvaluatorProvider
import com.intellij.debugger.engine.JavaValue.JavaFullValueEvaluator
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.CompoundReferenceRenderer
import com.intellij.debugger.ui.tree.render.CompoundRendererProvider
import com.intellij.debugger.ui.tree.render.NodeRenderer
import com.intellij.debugger.ui.tree.render.ValueIconRenderer
import com.intellij.ide.IdeBundle
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.sun.jdi.ObjectReference
import com.sun.jdi.Type
import com.sun.jdi.Value
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.debugger.createRendererName
import sap.commerce.toolset.debugger.findClass
import sap.commerce.toolset.debugger.getMeta
import sap.commerce.toolset.i18n
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class DefaultModelRenderer : CompoundRendererProvider() {

    override fun getName() = "[y] Model Renderer"
    override fun getClassName() = "de.hybris.platform.servicelayer.model.AbstractItemModel"
    override fun isEnabled() = true

    override fun getIsApplicableChecker(): Function<Type?, CompletableFuture<Boolean>> {
        return Function { t ->
            CompletableFuture.completedFuture(
                DebuggerUtils.instanceOf(t, className)
            )
        }
    }

    override fun getIconRenderer() = ValueIconRenderer { _, _, _ -> HybrisIcons.Y.LOGO_BLUE }

    override fun getFullValueEvaluatorProvider(): FullValueEvaluatorProvider {
        return FullValueEvaluatorProvider { evaluationContext: EvaluationContextImpl, valueDescriptor: ValueDescriptorImpl ->
            object : JavaFullValueEvaluator(i18n("hybris.debug.message.node.type.renderer.create"), evaluationContext) {
                override fun evaluate(callback: XFullValueEvaluationCallback) {
                    val value = valueDescriptor.value
                    val project = valueDescriptor.project
                    val classNameFqn = value.type().name()

                    application.runReadAction {
                        val meta = getMeta(project, classNameFqn)
                        val psiClass = findClass(project, classNameFqn)

                        // ensure that both meta and psiClass are available
                        if (meta == null || psiClass == null) return@runReadAction
                    }

                    if (DumbService.isDumb(project)) {
                        Notifications.create(
                            NotificationType.INFORMATION,
                            IdeBundle.message("progress.performing.indexing.tasks"),
                            i18n("hybris.notification.debug.dumb.mode.content")
                        )
                            .hideAfter(5)
                            .notify(project)
                        callback.errorOccurred(i18n("hybris.notification.debug.dumb.mode.content"))
                        return
                    }

                    val rendererName = createRendererName(classNameFqn)

                    NodeRendererSettings.getInstance().getAllRenderers(project)
                        .filterIsInstance<CompoundReferenceRenderer>()
                        .find { it.name == rendererName && it.className == classNameFqn }
                        ?: createCompoundReferenceRenderer(value, project, rendererName, classNameFqn)

                    callback.evaluated("")
                }

                override fun isShowValuePopup() = false
            }
        }
    }

    private fun createCompoundReferenceRenderer(
        value: Value,
        project: Project,
        rendererName: String,
        className: String
    ): NodeRenderer? {
        if (value !is ObjectReference) return null

        val renderer = ModelRenderer(className, project).createRenderer()
        renderer.name = rendererName

        with(NodeRendererSettings.getInstance()) {
            customRenderers.addRenderer(renderer)
            fireRenderersChanged()
        }

        return renderer
    }
}