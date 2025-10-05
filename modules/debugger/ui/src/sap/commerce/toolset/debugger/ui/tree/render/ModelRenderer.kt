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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.util.PsiNavigateUtil
import com.sun.jdi.ClassType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.debugger.toTypeCode
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class ModelRenderer : CompoundRendererProvider() {

    override fun getName() = "[y] Model Renderer"
    override fun getClassName() = "de.hybris.platform.servicelayer.model.AbstractItemModel"
    override fun isEnabled() = true
    override fun getChildrenRenderer(): ChildrenRenderer = ModelChildrenRenderer()
    override fun getIconRenderer() = ValueIconRenderer { x, y, t ->
        val typeCode = x.type?.name()?.toTypeCode() ?: return@ValueIconRenderer HybrisIcons.Y.LOGO_GREEN
        val meta = TSMetaModelAccess.getInstance(y.project).findMetaItemByName(typeCode) ?: return@ValueIconRenderer HybrisIcons.Y.LOGO_GREEN

        return@ValueIconRenderer when {
            meta.isCustom -> HybrisIcons.Y.LOGO_BLUE
            HybrisConstants.PLATFORM_EXTENSION_NAMES.contains(meta.extensionName) -> HybrisIcons.Y.LOGO_ORANGE
            else -> HybrisIcons.Y.LOGO_GREEN
        }
    }

    override fun getFullValueEvaluatorProvider(): FullValueEvaluatorProvider =
        FullValueEvaluatorProvider { evaluationContext: EvaluationContextImpl, valueDescriptor: ValueDescriptorImpl ->
            object : JavaFullValueEvaluator(JavaDebuggerBundle.message("message.node.navigate"), evaluationContext) {
                override fun evaluate(callback: XFullValueEvaluationCallback) {
                    val value = valueDescriptor.getValue()
                    val type = value.type() as ClassType
                    callback.evaluated("")

                    CoroutineScope(Dispatchers.Default).launch {
                        val name = type.name()
                        val psiClass = readAction { DebuggerUtils.findClass(name, valueDescriptor.project, evaluationContext.debugProcess.searchScope) }
                            ?: return@launch
                        val navigationElement = readAction { psiClass.navigationElement }
                        val navigatable = readAction { PsiNavigateUtil.getNavigatable(navigationElement) }
                            ?: return@launch

                        withContext(Dispatchers.EDT) {
                            navigatable.navigate(true)
//                            PsiNavigateUtil.navigate(navigationElement)
//                            DebuggerUIUtil.invokeLater { PsiNavigateUtil.navigate(navigationElement) }
                        }
                    }
                }

                override fun isShowValuePopup() = false
            }
        }
}
