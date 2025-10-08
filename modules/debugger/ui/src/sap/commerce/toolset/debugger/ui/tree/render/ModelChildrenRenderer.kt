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
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.impl.DebuggerUtilsAsync
import com.intellij.debugger.ui.impl.watch.MessageDescriptor
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.DebuggerTreeNode
import com.intellij.debugger.ui.tree.NodeDescriptor
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.ChildrenBuilder
import com.intellij.debugger.ui.tree.render.ChildrenRenderer
import com.intellij.debugger.ui.tree.render.ReferenceRenderer
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import com.sun.jdi.Method
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value
import sap.commerce.toolset.debugger.getMeta
import sap.commerce.toolset.debugger.toTypeCode
import sap.commerce.toolset.debugger.ui.tree.LazyMethodValueDescriptor
import sap.commerce.toolset.debugger.ui.tree.MethodValueDescriptor
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import java.util.concurrent.CompletableFuture

internal class ModelChildrenRenderer : ReferenceRenderer("de.hybris.platform.servicelayer.model.AbstractItemModel"), ChildrenRenderer {

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
        val parentDescriptor = builder.parentDescriptor as? ValueDescriptorImpl ?: return
        val nodeManager = builder.nodeManager
        val project = parentDescriptor.project
        val type = objectReference.referenceType()

        if (DumbService.isDumb(project)) {
            builder.addChildren(listOf(nodeManager.createMessageNode("Direct fields access is not available during the re-index...")), false)
            DebugProcessImpl.getDefaultRenderer(value).buildChildren(value, builder, evaluationContext)
            return
        }

        val meta = getMeta(project, type.name())
        if (meta == null) {
            builder.addChildren(listOf(nodeManager.createNode(MessageDescriptor("Item type is not available in the local type system.", MessageDescriptor.ERROR), evaluationContext)), false)
            DebugProcessImpl.getDefaultRenderer(value).buildChildren(value, builder, evaluationContext)
            return
        }
        val metaAccess = TSMetaModelAccess.getInstance(project)

        DebuggerUtilsAsync.allMethods(type).thenApply { allMethods ->
            val excluded = setOf("<init>", "writeReplace", "readResolve")

            val groupedMethods = allMethods
                .filter { method -> method.argumentTypeNames().isEmpty() }
                .filter { method -> !method.isAbstract }
                .filterNot { method -> excluded.contains(method.name()) }
                .filter { method -> method.declaringType().name() != "java.lang.Object" }
                .distinctBy { method -> method.name() }
                .groupBy { method -> method.declaringType().name() }

            groupedMethods.forEach { (type, methods) ->
                val typeName = type.toTypeCode()
                val descriptors = methods
                    .mapNotNull { method ->
                        val methodName = method.name()

                        attributeNodeDescriptor(project, meta, value, method, metaAccess, methodName)
                            ?: relationNodeDescriptor(project, meta, value, method, methodName)
                            ?: MethodValueDescriptor(value, method, methodName, project)
                    }
                val groupNode = nodeManager.createMessageNode("$typeName | ${descriptors.size} fields")
                val nodes = descriptors
                    .map { descriptor -> nodeManager.createNode(descriptor, evaluationContext) }

                builder.addChildren(listOf(groupNode), false)
                builder.addChildren(nodes, false)
            }

            builder.addChildren(listOf(nodeManager.createMessageNode("Fields")), false)
            DebugProcessImpl.getDefaultRenderer(value).buildChildren(value, builder, evaluationContext)
        }
    }

    private fun attributeNodeDescriptor(
        project: Project,
        meta: TSGlobalMetaItem,
        value: ObjectReference,
        method: Method,
        metaAccess: TSMetaModelAccess,
        methodName: String
    ): NodeDescriptor? {
        val attribute = (meta.allAttributes.values
            .find { attribute -> attribute.customGetters.contains(methodName) }
            ?: if (methodName.startsWith("get")) meta.allAttributes[methodName.substring("get".length)]
            else meta.allAttributes[methodName.substring("is".length)])
            ?: return null
        val attributeName = attribute.name

        return when {
            attribute.isDynamic -> LazyMethodValueDescriptor(value, method, "$attributeName (dynamic)", project, attribute.icon)

            attribute.isLocalized -> LazyMethodValueDescriptor(value, method, "$attributeName (localized)", project, attribute.icon)

            metaAccess.findMetaCollectionByName(attribute.type) != null -> LazyMethodValueDescriptor(
                value,
                method,
                "$attributeName (collection)",
                project,
                attribute.icon
            )

            metaAccess.findMetaMapByName(attribute.type) != null -> LazyMethodValueDescriptor(value, method, "$attributeName (map)", project, attribute.icon)

            else -> MethodValueDescriptor(value, method, attributeName, project, attribute.icon)
        }
    }

    private fun relationNodeDescriptor(project: Project, meta: TSGlobalMetaItem, value: ObjectReference, method: Method, methodName: String): NodeDescriptor? {
        val relation = (meta.allRelationEnds
            .find { attribute -> attribute.customGetters.contains(methodName) }
            ?: if (methodName.startsWith("get")) meta.allRelationEnds
                .find { it.name?.equals(methodName.substring("get".length), true) ?: false }
            else null)
            ?: return null

        return LazyMethodValueDescriptor(value, method, "${relation.name} (relation - ${relation.end.name.lowercase()})", project, relation.end.icon)
    }

    override fun getChildValueExpression(node: DebuggerTreeNode, context: DebuggerContext) = node.descriptor
        .asSafely<ValueDescriptor>()
        ?.getDescriptorEvaluation(context)
}