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

package sap.commerce.toolset.ccv2.manifest.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.manifest.jsonSchema.providers.ManifestCommerceJsonSchemaFileProvider
import sap.commerce.toolset.i18n

class ManifestCommerceExtensionPackInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        val file = holder.file

        if (!ManifestCommerceJsonSchemaFileProvider.instance(file.project).isAvailable(file.viewProvider.virtualFile)) return PsiElementVisitor.EMPTY_VISITOR

        return ManifestCommerceVisitor(holder)
    }

    class ManifestCommerceVisitor(val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitStringLiteral(o: JsonStringLiteral) {
            val parent = o.parent
            if (isApplicable(parent, o) && !CCv2Constants.COMMERCE_EXTENSION_PACKS.contains(o.value)) {
                holder.registerProblem(
                        o,
                        i18n("hybris.inspections.fix.manifest.ManifestUnknownExtensionPackInspection.message", o.value)
                )
            }
        }

        private fun isApplicable(parent: PsiElement?, o: JsonStringLiteral) = parent is JsonProperty
                && JsonPsiUtil.isPropertyValue(o)
                && parent.name == "name"
                && parent.parentOfType<JsonProperty>()?.name == "extensionPacks"

    }
}