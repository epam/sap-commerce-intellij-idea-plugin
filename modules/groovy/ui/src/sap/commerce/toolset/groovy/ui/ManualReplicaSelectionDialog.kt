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

package sap.commerce.toolset.groovy.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.groovy.GroovyExecConstants
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.exec.context.GroovyReplicaAwareContext
import java.awt.Component
import javax.swing.JComponent

class ManualReplicaSelectionDialog(
    private val project: Project,
    private val editor: Editor,
    private val currentSettings: GroovyExecContext.Settings,
    parentComponent: Component,
    private val replicaContext: GroovyReplicaAwareContext.Mutable = currentSettings.replicaContext
        .takeIf { it.replicaSelectionMode == GroovyExecConstants.manual }
        ?.mutable()
        ?: GroovyReplicaAwareContext(GroovyExecConstants.manual).mutable()
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE), Disposable {

    init {
        title = "Manual Replica Selection"
        isResizable = false
        super.init()
    }

    override fun dispose() {
        super.dispose()
    }

    override fun createCenterPanel(): JComponent {
        // TODO: support multiple replicas

        if (replicaContext.replicaContexts.size != 1) {
            replicaContext.replicaContexts.clear()
            replicaContext.replicaContexts.add(ReplicaContext("", "").mutable())
        }

        val singleReplicaContext = replicaContext.replicaContexts.first()
        return panel {
            row {
                cell(
                    InlineBanner(
                        """Possibility to specify multiple replicas is planned for future releases.<br>
                            Feel free to contribute to the Plugin. 
                        """.trimIndent(),
                        EditorNotificationPanel.Status.Info
                    )
                        .showCloseButton(false)
                )
                    .customize(UnscaledGaps(12, 12, 12, 12))
                    .align(Align.CENTER)
            }

            row {
                textField()
                    .label("Replica id:")
                    .bindText(singleReplicaContext::replicaId)
                    .align(AlignX.FILL)
                    .component
            }
                .layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .label("Cookie name:")
                    .bindText(singleReplicaContext::cookieName)
                    .align(AlignX.FILL)
                    .component
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    override fun applyFields() {
        super.applyFields()

        editor.groovyExecContextSettings = currentSettings.copy(replicaContext = replicaContext.immutable())
    }
}