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

package sap.commerce.toolset.debugger.engine.managerThread

import com.intellij.debugger.JavaDebuggerBundle
import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.impl.descriptors.data.UserExpressionData
import com.intellij.debugger.ui.impl.watch.UserExpressionDescriptorImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener
import com.intellij.debugger.ui.tree.render.ToStringCommand
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.rd.util.firstOrNull
import com.sun.jdi.ObjectReference
import com.sun.jdi.Type
import com.sun.jdi.Value
import sap.commerce.toolset.debugger.DebuggerConstants
import sap.commerce.toolset.debugger.toTypeCode
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

internal class ModelToStringCommand(
    private val valueDescriptor: ValueDescriptor,
    private val labelListener: DescriptorLabelListener,
    evaluationContext: EvaluationContext,
    value: ObjectReference
) : ToStringCommand(evaluationContext, value) {

    override fun evaluationResult(message: String?) {
        valueDescriptor.setValueLabel(StringUtil.notNullize(message))
        labelListener.labelChanged()
    }

    override fun evaluationError(message: String?) {
        val typeName = value.type().name()
        val msg = "$message " + JavaDebuggerBundle.message("evaluation.error.cannot.evaluate.tostring", typeName)
        valueDescriptor.setValueLabelFailed(EvaluateException(msg, null))
        labelListener.labelChanged()
    }

    override fun action() {
        val project = evaluationContext.project
        val expression = toStringExpression(value.type(), project).trimIndent()
        val text = DebuggerUtils.getInstance().createExpressionWithImports(expression)

        val descriptor = UserExpressionData(
            valueDescriptor as ValueDescriptorImpl,
            value.type().name(),
            "toString_renderer_" + value.type().name(),
            text
        )
            .createDescriptor(project) as UserExpressionDescriptorImpl

        try {
            val calcValue = descriptor.calcValue(evaluationContext as EvaluationContextImpl)
            val valueAsString = DebuggerUtils.getValueAsString(evaluationContext, calcValue)
            evaluationResult(valueAsString)
        } catch (_: EvaluateException) {
            fallback(evaluationContext, value)
        }
    }

    private fun fallback(evaluationContext: EvaluationContext, value: Value) {
        try {
            val valueAsString = DebuggerUtils.getValueAsString(evaluationContext, value)
            evaluationResult(valueAsString)
        } catch (e: EvaluateException) {
            evaluationError(e.message)
        }
    }

    private fun toStringExpression(type: Type, project: Project) = DebuggerConstants.TO_STRING_MAPPING
        .filterKeys { DebuggerUtils.instanceOf(type, it) }
        .firstOrNull()
        ?.value
        ?: fallbackToTypeSystem(type, project)
        ?: DebuggerConstants.ITEM;

    private fun fallbackToTypeSystem(type: Type, project: Project): String? {
        val typeCode = type.name().toTypeCode()
        val mappingTypecode2Getters = TSMetaModelAccess.getInstance(project).getTypecode2Mapping()

        val getters = mappingTypecode2Getters[typeCode]
            ?.take(3)
            ?.sorted()
            ?.joinToString(" + \" | \" + ") { it }
            ?: return null

        return """
            ${DebuggerConstants.PK}
            
            pk + " | " + $getters
        """.trimIndent()
    }
}