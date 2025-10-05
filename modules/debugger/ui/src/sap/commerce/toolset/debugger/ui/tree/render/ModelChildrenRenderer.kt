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

import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.impl.DebuggerUtilsAsync
import com.intellij.debugger.impl.descriptors.data.UserExpressionData
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.DebuggerTreeNode
import com.intellij.debugger.ui.tree.NodeDescriptor
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.ChildrenBuilder
import com.intellij.debugger.ui.tree.render.ChildrenRenderer
import com.intellij.debugger.ui.tree.render.OnDemandRenderer
import com.intellij.debugger.ui.tree.render.ReferenceRenderer
import com.intellij.util.asSafely
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value
import sap.commerce.toolset.debugger.createChildInfo
import sap.commerce.toolset.debugger.getMeta
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import java.util.concurrent.CompletableFuture

class ModelChildrenRenderer : ReferenceRenderer("de.hybris.platform.servicelayer.model.AbstractItemModel"), ChildrenRenderer {

    override fun getUniqueId() = "[y] Item Type Children Renderer"

    override fun isExpandableAsync(value: Value, evaluationContext: EvaluationContext, parentDescriptor: NodeDescriptor): CompletableFuture<Boolean> = DebugProcessImpl
        .getDefaultRenderer(value)
        .isExpandableAsync(value, evaluationContext, parentDescriptor)

    override fun buildChildren(
        value: Value,
        builder: ChildrenBuilder,
        evaluationContext: EvaluationContext
    ) {
        DebuggerManagerThreadImpl.assertIsManagerThread()

        val objectReference = value.asSafely<ObjectReference>() ?: return
        val debuggerUtils = DebuggerUtils.getInstance()
        val nodeManager = builder.nodeManager
        val descriptorFactory = builder.descriptorManager
        val parentDescriptor = builder.parentDescriptor as ValueDescriptorImpl
        val project = parentDescriptor.project
        val type = objectReference.referenceType()
        val meta = getMeta(project, type.name()) ?: return
        val metaAccess = TSMetaModelAccess.getInstance(project)

        DebuggerUtilsAsync.allFields(type).thenApply { allFields ->
            val children = allFields.filterNot { it.name().startsWith("_") }
                .mapIndexedNotNull { index, field ->
                    val attributeName = field.name()

                    val info = (meta.allAttributes[attributeName]
                        ?.takeIf { it.modifiers.isRead }
                        ?.let { attribute ->
                            createChildInfo(attribute, attribute.name, attributeName, metaAccess, debuggerUtils) }
                        ?: meta.allRelationEnds
                            .filter { relation -> relation.isNavigable }
                            .find { relation -> relation.name.equals(attributeName, true) }
                            ?.takeIf { it.modifiers.isRead }
                            ?.let { relation -> createChildInfo(relation.name!!, relation, attributeName, debuggerUtils) }
                        )
                        ?: return@mapIndexedNotNull null

                    UserExpressionData(parentDescriptor, type.name(), info.myName, info.myExpression)
                        .also { it.setEnumerationIndex(index) }
                        .let { descriptorFactory.getUserExpressionDescriptor(parentDescriptor, it) }
                        .also { if (info.myOnDemand) it.putUserData(OnDemandRenderer.ON_DEMAND_CALCULATED, false) }
                        .let { nodeManager.createNode(it, evaluationContext) }
                }

            builder.addChildren(children, false)

            DebugProcessImpl.getDefaultRenderer(value).buildChildren(value, builder, evaluationContext)
        }
    }

    override fun getChildValueExpression(node: DebuggerTreeNode, context: DebuggerContext) = node.descriptor
        .asSafely<ValueDescriptor>()
        ?.getDescriptorEvaluation(context)
}