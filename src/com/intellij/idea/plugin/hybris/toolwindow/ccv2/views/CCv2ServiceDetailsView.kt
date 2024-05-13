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

import com.intellij.ide.HelpTooltip
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.GridBagLayout
import java.awt.datatransfer.StringSelection
import java.io.Serial

class CCv2ServiceDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private var environment: CCv2EnvironmentDto,
    private var service: CCv2ServiceDto,
) : SimpleToolWindowPanel(false, true), Disposable {

    private val showProperties = AtomicBooleanProperty(service.properties != null)

    private val propertiesPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private var rootPanel = rootPanel()

    override fun dispose() {
        // NOP
    }

    init {
        initPanel()
    }

    private fun initPanel() {
        add(rootPanel)

        initPropertiesPanel()
    }

    private fun initPropertiesPanel() {
        val properties = service.properties
        if (properties == null) {
            CCv2Service.getInstance(project).fetchEnvironmentServiceProperties(subscription, environment, service,
                {
                    showProperties.set(false)
                    service.properties = null
                    propertiesPanel.removeAll()
                },
                {
                    service.properties = it

                    invokeLater {
                        showProperties.set(it != null)

                        if (it != null) {
                            propertiesPanel.add(propertiesPanel(it))
                        }
                    }
                }
            )
        } else {
            propertiesPanel.removeAll()
            propertiesPanel.add(propertiesPanel(properties))
        }
    }

    private fun propertiesPanel(properties: Map<String, String>) = panel {
        properties.forEach { (key, value) ->
            row {
                panel {
                    row {
                        link(key) {
                            CopyPasteManager.getInstance().setContents(StringSelection(key))
                            Notifications.create(NotificationType.INFORMATION, "Key copied to clipboard", "")
                                .hideAfter(10)
                                .notify(project)
                        }
                            .applyToComponent {
                                HelpTooltip()
                                    .setTitle("Click to copy to clipboard")
                                    .installOn(this);
                            }
                    }
                }.gap(RightGap.SMALL)

                panel {
                    row {
                        link(value) {
                            CopyPasteManager.getInstance().setContents(StringSelection(value))
                            Notifications.create(NotificationType.INFORMATION, "Value copied to clipboard", "")
                                .hideAfter(10)
                                .notify(project)
                        }
                            .applyToComponent {
                                HelpTooltip()
                                    .setTitle("Click to copy to clipboard")
                                    .installOn(this);
                            }
                    }
                }
            }
                .layout(RowLayout.PARENT_GRID)
        }
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

            collapsibleGroup("Properties") {
                row {
                    cell(propertiesPanel)
                }.visibleIf(showProperties)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving service properties...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showProperties.not())
            }.expanded = true
        }
    }
        .let { Dsl.scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = 1808556418262990847L
    }

}