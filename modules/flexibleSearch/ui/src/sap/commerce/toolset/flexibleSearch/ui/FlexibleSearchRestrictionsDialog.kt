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

package sap.commerce.toolset.flexibleSearch.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.flexibleSearch.restrictions.FlexibleSearchRestriction
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class FlexibleSearchRestrictionsDialog(
    private val project: Project,
    private val userUid: String,
    private val restrictions: Collection<FlexibleSearchRestriction>,
) : DialogWrapper(project, null, false, IdeModalityType.IDE) {

    init {
        title = "FlexibleSearch Restrictions"
        super.init()
    }

    override fun getInitialSize() = JBUI.DialogSizes.large()

    override fun createCenterPanel() = panel {
        row {
            text("The following search restrictions have been identified for the <strong>$userUid</strong> and the given FlexibleSearch query.")
        }

        row {
            scrollCell(restrictions())
                .resizableColumn()
                .align(Align.FILL)
        }
            .resizableRow()
            .topGap(TopGap.MEDIUM)
    }.apply {
        preferredSize = JBUI.DialogSizes.large()
    }

    private fun restrictions() = panel {
        val metaModelAccess = TSMetaModelAccess.getInstance(project)

        restrictions.forEach { restriction ->
            row {
                icon(HybrisIcons.FlexibleSearch.RESTRICTIONS)
                    .gap(RightGap.SMALL)
                label(restriction.code)
                    .comment("Restriction")
                    .gap(RightGap.COLUMNS)

                val typeIcon = (metaModelAccess.findMetaClassifierByName(restriction.typeCode)
                    ?.icon
                    ?: HybrisIcons.TypeSystem.Types.ITEM)

                icon(typeIcon)
                    .gap(RightGap.SMALL)
                label(restriction.typeCode)
                    .comment("Type")
                    .gap(RightGap.COLUMNS)
            }.layout(RowLayout.PARENT_GRID)

            row {
                textArea()
                    .rows(3)
                    .text(restriction.query)
                    .align(Align.FILL)
            }.layout(RowLayout.PARENT_GRID)
        }
    }
}