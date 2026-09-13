/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.codeInspection.fix.CxOpenWinningDeclarationFix
import sap.commerce.toolset.properties.codeInspection.fix.CxRemovePropertyDeclarationFix
import sap.commerce.toolset.properties.meta.CxProperty
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertySource

/**
 * Reports a declaration which never applies, in the two cases where that is a surprise.
 *
 * `local.properties` beating an extension is the whole point of `local.properties`, and flagging it would bury the
 * file in warnings, so it is left alone. What is worth saying is that two extensions declare the same key and the
 * load order silently picks one of them, or that a declaration sits in an extension `localextensions.xml` never
 * lists - in which case the platform does not read the file at all and the value is simply not there.
 */
class CxShadowedPropertyInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            if (file.project.isNotHybrisProject) return

            val propertiesFile = file.asSafely<PropertiesFile>() ?: return
            val virtualFile = file.originalFile.virtualFile ?: return
            val chain = CxPropertyCollector.getInstance(file.project).collect()
            val source = chain.sources.find { it.file == virtualFile } ?: return

            propertiesFile.properties.forEach { property ->
                val key = property.key ?: return@forEach
                val collected = chain[key] ?: return@forEach
                val problem = problemOf(collected, source) ?: return@forEach

                holder.registerProblem(
                    property.psiElement,
                    keyRangeOf(property, key),
                    problem,
                    *fixesFor(collected),
                )
            }
        }
    }

    private fun problemOf(property: CxProperty, source: CxPropertySource): String? {
        val winner = property.declaration

        return when {
            // Nothing active declares it at all, so the value only looks like it is configured.
            !source.active -> "'${property.key}' is declared in an extension localextensions.xml does not list, " +
                "so the running system never reads this value"

            winner == null || winner.source == source -> null
            // A different tier winning is the chain working as designed; the same tier is the load order deciding
            // something nobody wrote down.
            winner.source.scope != source.scope -> null

            else -> "'${property.key}' is also declared in ${winner.source.presentableName}, which wins by extension " +
                "load order - this value never applies"
        }
    }

    private fun fixesFor(property: CxProperty): Array<LocalQuickFix> {
        val winner = property.declaration ?: return arrayOf(CxRemovePropertyDeclarationFix())

        return arrayOf(
            CxOpenWinningDeclarationFix(property.key, winner.source.presentableName),
            CxRemovePropertyDeclarationFix(),
        )
    }

    /** Underlines the key alone, the way the catalogue inspections do. */
    private fun keyRangeOf(property: IProperty, key: String): TextRange {
        val element = property.psiElement
        val start = element.text.indexOf(key).takeIf { it >= 0 } ?: return TextRange(0, element.textLength)

        return TextRange(start, start + key.length)
    }
}
