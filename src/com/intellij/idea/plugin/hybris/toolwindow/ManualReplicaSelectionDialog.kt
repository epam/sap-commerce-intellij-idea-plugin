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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaContext
import com.intellij.idea.plugin.hybris.tools.remote.http.RemoteConnectionContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import java.awt.Component
import javax.swing.JComponent

class ManualReplicaSelectionDialog(
    private val project: Project,
    private val replicas: Collection<ReplicaContext>,
    parentComponent: Component
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE), Disposable {

    init {
        title = "Manual Replica Selection"
        isResizable = false
        super.init()
    }

    override fun dispose() {
        super.dispose()
    }

    private lateinit var manualCookieName: JBTextField
    private lateinit var manualReplicaId: JBTextField

    override fun createCenterPanel(): JComponent {
        // TODO: support multiple replicas
        val firstReplica = replicas.firstOrNull()
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
                manualReplicaId = textField()
                    .label("Replica id:")
                    .text(firstReplica?.replicaId ?: "")
                    .align(AlignX.FILL)
                    .component
            }
                .layout(RowLayout.PARENT_GRID)

            row {
                manualCookieName = textField()
                    .label("Cookie name:")
                    .text(firstReplica?.cookieName ?: "")
                    .align(AlignX.FILL)
                    .component
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    override fun applyFields() {
        super.applyFields()

        val replicaContext = ReplicaContext(manualReplicaId.text, manualCookieName.text)

        project.service<GroovyExecutionClient>().connectionContext = RemoteConnectionContext.manual(listOf(replicaContext))
    }
}
