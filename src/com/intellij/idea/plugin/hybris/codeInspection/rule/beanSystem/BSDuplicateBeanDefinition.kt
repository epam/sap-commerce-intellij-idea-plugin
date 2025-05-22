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

package com.intellij.idea.plugin.hybris.codeInspection.rule.beanSystem

import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class BSDuplicateBeanDefinition : AbstractBSInspection() {

    override fun inspect(
        project: Project,
        dom: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val metaModelAccess = project.service<BSMetaModelAccess>()

        dom.beans
            .forEach { inspect(it, holder, severity, metaModelAccess) }
    }

    private fun inspect(
        dom: Bean,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        metaModelAccess: BSMetaModelAccess
    ) {
        val metas = metaModelAccess.findMetasForDom(dom)

        if (metas.isEmpty()) return

        val currentFileDeclarations = metas
            .flatMap { it.declarations }
            .map { it.domAnchor }
            .filter { it.containingFile == dom.xmlElement?.containingFile }
            .mapNotNull { it.retrieveDomElement() }
        if (currentFileDeclarations.size > 1) {
            holder.createProblem(
                dom.clazz,
                severity,
                displayName
            )
        }
    }
}