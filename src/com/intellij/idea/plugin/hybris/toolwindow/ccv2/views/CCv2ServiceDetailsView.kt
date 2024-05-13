/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2.views

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.io.Serial

class CCv2ServiceDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private var environment: CCv2EnvironmentDto,
    private var service: CCv2ServiceDto,
) : SimpleToolWindowPanel(false, true), Disposable {

    override fun dispose() {
        // NOP
    }

    init {
        add(rootPanel())
    }

    private fun rootPanel() = panel {
        indent {
            row {
                label("${environment.name} - ${service.name}")
                    .comment("Service")
                    .bold()
                    .component.also {
                        it.font = JBUI.Fonts.label(26f)
                    }
            }
                .topGap(TopGap.SMALL)
                .bottomGap(BottomGap.SMALL)

            row {
                panel {
                    ccv2ServiceStatusRow(service)
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceReplicasRow(service)
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceModifiedByRow(service)
                }
                    .gap(RightGap.COLUMNS)
            }

            collapsibleGroup("Replicas") {
                val replicas = service.replicas
                if (replicas.isEmpty()) {
                    row {
                        label("No replicas found for environment.")
                            .align(Align.FILL)
                    }
                } else {
                    replicas.forEach { replica ->
                        row {
                            panel {
                                row {
                                    label(replica.name)
                                        .bold()
                                        .comment("Name")
                                }
                            }.gap(RightGap.COLUMNS)

                            panel {
                                row {
                                    label(replica.status)
                                        .comment("Status")
                                }
                            }
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }
            }

//            collapsibleGroup("Properties") {
//                row {
//                    cell(JBTable())
//                }
//            }.expanded = true
        }
    }
        .let { Dsl.scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = 1808556418262990847L
    }

}