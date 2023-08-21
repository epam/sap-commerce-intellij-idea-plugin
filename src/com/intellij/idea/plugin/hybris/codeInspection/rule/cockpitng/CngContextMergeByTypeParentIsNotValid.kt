/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.cockpitng

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class CngContextMergeByTypeParentIsNotValid : AbstractCngConfigInspection() {

    override fun inspect(
        project: Project,
        dom: Config,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val metaModelAccess = TSMetaModelAccess.getInstance(project)
        dom.contexts
            .filter { it.mergeBy.stringValue == "type" }
            .forEach { check(it, holder, severity, metaModelAccess) }
    }

    private fun check(
        dom: Context,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        metaModelAccess: TSMetaModelAccess
    ) {
        val parentType = dom.parentAttribute.stringValue ?: return
        val type = dom.type.stringValue ?: return

        val typeMeta = metaModelAccess.findMetaClassifierByName(type) ?: return

        // skip enums
        if (typeMeta is TSGlobalMetaEnum && parentType == HybrisConstants.TS_TYPE_ENUMERATION_VALUE) return

        // skip non-item types
        if (typeMeta !is TSGlobalMetaItem) return

        val validParentType = typeMeta.allExtends
            .any { parentType.equals(it.name, true) }

        if (validParentType) return

        holder.createProblem(
            dom.parentAttribute,
            severity,
            message("hybris.inspections.fix.cng.CngContextMergeByTypeParentIsNotValid.key", type, parentType)
        )
    }
}