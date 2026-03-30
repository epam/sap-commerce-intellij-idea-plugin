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

package sap.commerce.toolset.impex.ui.components

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.equalsTo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.dsl.builder.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationContext
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationMode
import java.awt.Component

class ImpExDocIdGenerationDialog(
    project: Project,
    parentComponent: Component,
    private val mutableContext: ImpExDocIdGenerationContext.Mutable
) : DialogWrapper(project, parentComponent, false, IdeModalityType.PROJECT) {

    private val modeObservable = AtomicProperty(mutableContext.mode)

    init {
        title = "Generate DocId for ImpEx"
        isResizable = false

        super.init()
    }

    override fun createCenterPanel() = panel {
        row {
            label("Mode:")
                .bold()
            segmentedButton(
                ImpExDocIdGenerationMode.entries.toList()
            ) {
                icon = it.icon
                text = it.title
                toolTipText = it.description
            }
                .bind(modeObservable)
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label("Doc id:")
                .bindText(mutableContext::name)
                .align(AlignX.FILL)
                .comment("Represents name of the newly generated &docId column.")
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label("Prefix:")
                .bindText(mutableContext::prefix)
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label("Postfix:")
                .bindText(mutableContext::postfix)
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)

        group("Columns") {
            mutableContext.columns.forEach { column ->
                row {
                    label("#${column.number + 1}")
                        .gap(RightGap.SMALL)

                    checkBox(StringUtil.shortenPathWithEllipsis(column.name, 30))
                        .bindSelected(column::include)
                        .align(AlignX.FILL)
                        .gap(RightGap.SMALL)

                    if (column.unique) {
                        icon(HybrisIcons.ImpEx.COLUMN_UNIQUE)
                    }
                }
            }
        }.enabledIf(modeObservable.equalsTo(ImpExDocIdGenerationMode.COLUMN_BASED))
    }

    fun get() = if (showAndGet()) mutableContext
        .apply { mode = modeObservable.get() }
        .immutable()
    else null
}
