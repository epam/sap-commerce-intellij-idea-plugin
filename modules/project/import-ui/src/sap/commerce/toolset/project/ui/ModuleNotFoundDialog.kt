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

package sap.commerce.toolset.project.ui

import com.intellij.CommonBundle
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.ui.banner
import java.awt.Dimension
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import kotlin.io.path.pathString

class ModuleNotFoundDialog(
    private val message: String,
    private val moduleDescriptors: Collection<ModuleDescriptor>
) : DialogWrapper(false) {

    private val ui = ClearableLazyValue.create {
        panel {
            row {
                text(
                    """
                    Please review identified module descriptors.
                    It may happen that specific module was not found because it is located within another module.
                """.trimIndent()
                )
            }

            moduleDescriptors.groupBy { it.type }
                .forEach { (type, descriptors) ->
                    group(JBLabel(type.title, type.icon, SwingConstants.LEFT)) {
                        descriptors.forEach { descriptor ->
                            row {
                                label(descriptor.name)
                                label(descriptor.moduleRootPath.pathString)
                            }.layout(RowLayout.PARENT_GRID)
                        }
                    }
                }
        }.apply {
            border = JBUI.Borders.empty(8, 16, 32, 16)
        }
    }

    init {
        title = "Mandatory Module Not Found"
        isResizable = false
        setCancelButtonText(CommonBundle.getCloseButtonText())
        super.init()
    }

    override fun getStyle() = DialogStyle.COMPACT

    override fun createNorthPanel() = banner(
        text = message,
        status = EditorNotificationPanel.Status.Error
    )

    override fun createCenterPanel() = panel {
        row {
            scrollCell(ui.value)
                .align(Align.FILL)
                .applyToComponent {
                    parent.parent.asSafely<JBScrollPane>()
                        ?.apply {
                            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                            border = JBEmptyBorder(0)
                        }
                }

        }.resizableRow()
    }
        .apply {
            preferredSize = Dimension(JBUIScale.scale(600), JBUIScale.scale(400))
        }

    override fun createActions() = arrayOf(cancelAction)
}
