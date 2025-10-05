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

package sap.commerce.toolset.debugger.ui.tree

import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.DescriptorWithParentObject
import com.intellij.openapi.project.Project
import com.sun.jdi.Method
import com.sun.jdi.ObjectReference
import javax.swing.Icon

open class MethodValueDescriptor(
    private val parentObject: ObjectReference,
    private val method: Method,
    private val presentationName: String,
    project: Project,
    private val icon: Icon? = null,
) : ValueDescriptorImpl(project), DescriptorWithParentObject {

    override fun calcValue(evaluationContext: EvaluationContextImpl?) = evaluationContext
        ?.debugProcess
        ?.invokeMethod(evaluationContext, parentObject, method, emptyList())

    override fun getName() = presentationName
    override fun getObject() = parentObject
    override fun getDescriptorEvaluation(context: DebuggerContext?) = throw EvaluateException("Getter evaluation is not supported")
    override fun getValueIcon() = icon
}