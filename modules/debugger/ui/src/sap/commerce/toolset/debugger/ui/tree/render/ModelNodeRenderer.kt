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
import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener
import com.intellij.debugger.ui.tree.render.ReferenceRenderer
import com.intellij.debugger.ui.tree.render.ToStringRenderer
import com.intellij.debugger.ui.tree.render.ValueLabelRenderer
import com.intellij.xdebugger.impl.ui.XDebuggerUIConstants
import com.sun.jdi.ObjectReference
import sap.commerce.toolset.debugger.engine.managerThread.ModelToStringCommand

internal class ModelNodeRenderer : ReferenceRenderer(), ValueLabelRenderer {

    private val fallbackRenderer by lazy { ToStringRenderer() }

    override fun getUniqueId() = "[y] Model Renderer"

    override fun calcLabel(
        valueDescriptor: ValueDescriptor,
        evaluationContext: EvaluationContext,
        labelListener: DescriptorLabelListener
    ): String? {
        val value = valueDescriptor.value as? ObjectReference
            ?: return fallbackRenderer.calcLabel(valueDescriptor, evaluationContext, labelListener)

        DebuggerUtils.ensureNotInsideObjectConstructor(value, evaluationContext)

        val suspendContext = evaluationContext.suspendContext as SuspendContextImpl
        suspendContext.managerThread.invokeCommand(ModelToStringCommand(valueDescriptor, labelListener, evaluationContext, value))


        return XDebuggerUIConstants.getCollectingDataMessage()
    }
}