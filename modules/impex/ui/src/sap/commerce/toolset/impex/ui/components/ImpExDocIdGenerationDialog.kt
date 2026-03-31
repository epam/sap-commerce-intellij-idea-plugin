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

import com.intellij.openapi.observable.util.equalsTo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationContext
import sap.commerce.toolset.impex.codeInspection.context.ImpExDocIdGenerationMode
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.ui.previewEditor
import java.awt.Component
import java.awt.Dimension
import javax.swing.ScrollPaneConstants

class ImpExDocIdGenerationDialog(
    private val project: Project,
    parentComponent: Component,
    private val mutableContext: ImpExDocIdGenerationContext.Mutable
) : DialogWrapper(project, parentComponent, false, IdeModalityType.PROJECT) {

    init {
        title = "Generate DocId for ImpEx"
        isResizable = false

        super.init()
    }

    override fun createCenterPanel() = panel {
        twoColumnsRow(
            {
                panel {
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
                            .bind(mutableContext.modeProperty)
                    }.layout(RowLayout.PARENT_GRID)

                    row {
                        textField()
                            .label("Doc id:")
                            .bindText(mutableContext.nameProperty)
                            .align(AlignX.FILL)
                            .comment("Represents name of the newly generated &docId column.")
                    }.layout(RowLayout.PARENT_GRID)

                    row {
                        textField()
                            .label("Prefix:")
                            .bindText(mutableContext.prefixProperty)
                            .align(AlignX.FILL)
                    }.layout(RowLayout.PARENT_GRID)

                    row {
                        textField()
                            .label("Postfix:")
                            .bindText(mutableContext.postfixProperty)
                            .align(AlignX.FILL)
                    }.layout(RowLayout.PARENT_GRID)
                }
            },
            {
                panel {
                    row {
                        label("Columns")
                    }
                    mutableContext.columns.forEach { column ->
                        row {
                            label("#${column.numberProperty.get() + 1}")
                                .gap(RightGap.SMALL)

                            checkBox(StringUtil.shortenPathWithEllipsis(column.nameProperty.get(), 30))
                                .bindSelected(column.includeProperty)
                                .align(AlignX.FILL)
                                .gap(RightGap.SMALL)

                            if (column.uniqueProperty.get()) {
                                icon(HybrisIcons.ImpEx.COLUMN_UNIQUE)
                            }
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }.enabledIf(mutableContext.modeProperty.equalsTo(ImpExDocIdGenerationMode.COLUMN_BASED))
            }
        )

        separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)

        row {
            label("Preview")
                .align(AlignX.CENTER)
        }
        row {
            previewEditor(project, ImpExFileType) {
                setHorizontalScrollbarVisible(true)
                setCaretEnabled(true)
                scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                component.preferredSize = Dimension(JBUI.DialogSizes.large().width, 380)
                colorsScheme = createBoundColorSchemeDelegate(DebuggerUIUtil.getColorScheme())
            }
                .applyToComponent {
                    this.text = mutableContext.computePreview(mutableContext)

                    mutableContext.previewProperty.afterChange(disposable) {
                        this.text = it
                    }
                }
                .align(AlignX.FILL)
        }
    }

    fun get() = if (showAndGet()) mutableContext.immutable()
    else null
}
