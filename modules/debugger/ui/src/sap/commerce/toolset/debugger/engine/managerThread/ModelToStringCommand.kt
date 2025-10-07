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
import com.intellij.openapi.util.text.StringUtil
import com.sun.jdi.ObjectReference
import com.sun.jdi.Type

class ModelToStringCommand(
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
        val msg = "$message " + JavaDebuggerBundle.message(
            "evaluation.error.cannot.evaluate.tostring",
            value.type().name()
        )
        valueDescriptor.setValueLabelFailed(EvaluateException(msg, null))
        labelListener.labelChanged()
    }

    override fun action() {
        val project = evaluationContext.project
        val expression = getExpression(value.type())
        val text = DebuggerUtils.getInstance().createExpressionWithImports(expression.trimIndent())

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
        } catch (e: EvaluateException) {
            evaluationError(e.message)
        }
    }

    private fun getExpression(type: Type) = when {
        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.security.PrincipalModel") -> """
                    String toString = toString();
                    String uid = getUid();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);

                    pk + " | " + uid
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.product.ProductModel") -> """
                    String toString = toString();
                    String code = getCode() == null ? "?" : getCode();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);

                    if (getCatalogVersion() == null) return pk + " | " + code;

                    String catalogId = getCatalogVersion().getCatalog() == null ? "?" : getCatalogVersion().getCatalog().getId();
                    String version = getCatalogVersion().getVersion() == null ? "?" : getCatalogVersion().getVersion();
                    
                    pk + " | " + code + " | " + catalogId + " ("+ version + ")"
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.catalog.model.CatalogVersionModel") -> """
                    String toString = toString();
                    String catalogId = getCatalog() == null ? "?" : getCatalog().getId();
                    String version = getVersion() == null ? "?" : getVersion();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);

                    pk + " | " + catalogId + " ("+ version + ")"
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.order.AbstractOrderModel") -> """
                    String toString = toString();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);
                    String code = getCode() == null ? "?" : getCode();
                    String userUid = getUser() == null ? "?" : getUser().getUid();

                    pk + " | " + code + " | " + userUid
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.order.AbstractOrderEntryModel") -> """
                    String toString = toString();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);
                    Long quantity = getQuantity() == null ? "?" : getQuantity();
                    de.hybris.platform.core.model.product.ProductModel product = getProduct()

                    if (product == null) return pk + " | " + quantity + " ?";

                    String productCode = product.getCode() == null ? "?" : product.getCode();
                    String catalogId = product.getCatalogVersion().getCatalog() == null ? "?" : product.getCatalogVersion().getCatalog().getId();
                    String version = product.getCatalogVersion().getVersion() == null ? "?" : product.getCatalogVersion().getVersion();
                    
                    pk + " | " + quantity + " " + productCode + " | " + catalogId + " ("+ version + ")"
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.media.MediaModel") -> """
                    String toString = toString();
                    String code = getCode() == null ? "?" : getCode();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);

                    if (getCatalogVersion() == null) return pk + " | " + code;

                    String catalogId = getCatalogVersion().getCatalog() == null ? "?" : getCatalogVersion().getCatalog().getId();
                    String version = getCatalogVersion().getVersion() == null ? "?" : getCatalogVersion().getVersion();

                    pk + " | " + code + " | " + catalogId + " ("+ version + ")"
                """

        DebuggerUtils.instanceOf(type, "de.hybris.platform.core.model.c2l.C2LItemModel") -> """
                    String toString = toString();
                    String pk = toString.substring(toString.indexOf("(") + 1, toString.length()-1);
                    String isocode = getIsocode() == null ? "?" : getIsocode();

                    pk + " | " + isocode
                """

        else -> """
                    String toString = toString();
                    toString.substring(toString.indexOf("(") + 1, toString.length()-1);
                """

    }
}