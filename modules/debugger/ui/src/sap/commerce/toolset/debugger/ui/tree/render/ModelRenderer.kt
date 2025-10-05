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

import com.intellij.debugger.JavaDebuggerBundle
import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.FullValueEvaluatorProvider
import com.intellij.debugger.engine.JavaValue.JavaFullValueEvaluator
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.ChildrenRenderer
import com.intellij.debugger.ui.tree.render.CompoundRendererProvider
import com.intellij.debugger.ui.tree.render.ValueIconRenderer
import com.intellij.util.PsiNavigateUtil
import com.intellij.util.application
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil
import com.sun.jdi.ClassType
import sap.commerce.toolset.HybrisIcons

class ModelRenderer : CompoundRendererProvider() {

    override fun getName() = "[y] Model Renderer"
    override fun getClassName() = "de.hybris.platform.servicelayer.model.AbstractItemModel"
    override fun isEnabled() = true
    override fun getChildrenRenderer(): ChildrenRenderer = ModelChildrenRenderer()
    override fun getIconRenderer() = ValueIconRenderer { _, _, _ -> HybrisIcons.Y.LOGO_BLUE }

    override fun getFullValueEvaluatorProvider(): FullValueEvaluatorProvider =
        FullValueEvaluatorProvider { evaluationContext: EvaluationContextImpl, valueDescriptor: ValueDescriptorImpl ->
            object : JavaFullValueEvaluator(JavaDebuggerBundle.message("message.node.navigate"), evaluationContext) {
                override fun evaluate(callback: XFullValueEvaluationCallback) {
                    val value = valueDescriptor.getValue()
                    val type = value.type() as ClassType
                    callback.evaluated("")
                    application.runReadAction {
                        val psiClass = DebuggerUtils.findClass(type.name(), valueDescriptor.project, evaluationContext!!.debugProcess.searchScope)
                        if (psiClass != null) DebuggerUIUtil.invokeLater { PsiNavigateUtil.navigate(psiClass) }
                    }
                }

                override fun isShowValuePopup() = false
            }
        }
}
