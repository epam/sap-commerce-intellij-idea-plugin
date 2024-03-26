/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.manifest

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers.ManifestCommerceJsonSchemaFileProvider
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType

class ManifestCommerceTemplateExtensionInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        val file = holder.file

        if (!ManifestCommerceJsonSchemaFileProvider.instance(file.project).isAvailable(file.viewProvider.virtualFile)) return PsiElementVisitor.EMPTY_VISITOR

        return ManifestCommerceVisitor(holder)
    }

    class ManifestCommerceVisitor(val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitStringLiteral(o: JsonStringLiteral) {
            val parent = o.parent
            if (isApplicable(parent, o) && !ProjectSettingsComponent.getInstance(o.project).getAvailableExtensions().contains(o.value)) {
                holder.registerProblem(
                    o,
                    HybrisI18NBundleUtils.message("hybris.inspections.fix.manifest.ManifestUnknownTemplateExtensionInspection.message", o.value)
                )
            }
        }

        private fun isApplicable(parent: PsiElement, o: JsonStringLiteral) = (parent is JsonProperty
            && JsonPsiUtil.isPropertyValue(o)
            && parent.name == "template"
            && parent.parentOfType<JsonProperty>()?.name == "storefrontAddons")
            || (JsonPsiUtil.isArrayElement(o)
            && parent.parentOfType<JsonProperty>()
            ?.takeIf { it.name == "addons" || it.name == "storefronts" }
            ?.parentOfType<JsonProperty>()
            ?.name == "storefrontAddons"
            )

    }
}