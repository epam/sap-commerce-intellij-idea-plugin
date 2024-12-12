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

import com.intellij.database.script.generator.concatWithSpace
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.*
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildStatus
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.date
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.sUser
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.GridBagLayout
import java.io.Serial
import javax.swing.SwingConstants.CENTER

class CCv2BuildDetailsView(
    private val project: Project, private val subscription: CCv2Subscription, private var build: CCv2BuildDto
) : SimpleToolWindowPanel(false, true), Disposable {

    private val deploymentHistoryPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val showBuildHistory = AtomicBooleanProperty(false)
    private var rootPanel = rootPanel()
    private var isBuildInProgress = build.status == CCv2BuildStatus.BUILDING

    override fun dispose() {
        // NOP
    }

    init {
        installToolbar()
        initPanel()
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(actionManager.getAction("ccv2.environment.toolbar.actions"))

            add(CCv2RedoBuildAction(subscription, build))
            if (build.canDeploy()) {
                add(CCv2DeployBuildAction(subscription, build))
            }
            if (build.canDelete()) {
                add(CCv2DeleteBuildAction(subscription, build))
            }
            if (build.canDownloadLogs()) {
                add(CCv2DownloadBuildLogsAction(subscription, build))
            }
            add(
                CCv2FetchBuildDetailsAction(subscription, build, {}, {
                    build = it

                    this@CCv2BuildDetailsView.remove(rootPanel)
                    rootPanel = rootPanel()

                    initPanel()
                })
            )
            actionManager.createActionToolbar("SAP_CX_CCv2_ENVIRONMENT_${System.identityHashCode(build)}", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initPanel() {
        add(rootPanel)
        initDeploymentHistoryPanel()
    }

    private fun initDeploymentHistoryPanel() {

        CCv2Service.getInstance(project).fetchDeploymentsForBuild(subscription, build.code,
            {
                showBuildHistory.set(false)
                deploymentHistoryPanel.removeAll()
            }
        ) { deployments ->
            if (deployments.isNotEmpty()) {
                invokeLater {
                    showBuildHistory.set(true)

                    val groupedDeployments = deployments
                        .groupBy { it.envCode }
                    deploymentHistoryPanel.add(buildDeploymentHistoryPanel(groupedDeployments))
                }
            } else {
                invokeLater {
                    showBuildHistory.set(true)
                    deploymentHistoryPanel.add(panel {
                        row {
                            label("No deployments available for the build")
                        }
                    })
                }
            }
        }
    }

    private fun rootPanel() = panel {
        row {
            label("$subscription - ${build.code}").comment("Build").bold().component.also {
                it.font = JBUI.Fonts.label(26f)
            }.horizontalAlignment = CENTER
        }.topGap(TopGap.SMALL).bottomGap(BottomGap.SMALL)
        group(build.code) {
            row {
                panel {
                    row {
                        val buildName = build.link?.let { browserLink(build.name, it) } ?: label(build.name)
                        buildName.comment(build.code).bold()
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        label(build.version).comment("Version")
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        icon(HybrisIcons.CCv2.Build.BRANCH).gap(RightGap.SMALL)
                        label(build.branch).comment("Branch")
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        icon(build.status.icon).gap(RightGap.SMALL)
                        label(build.status.title).comment("Status")
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        sUser(project, build.createdBy, HybrisIcons.CCv2.Build.CREATED_BY)
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        date("Start time", build.startTime)
                    }
                }.gap(RightGap.COLUMNS)

                panel {
                    row {
                        date("End time", build.endTime)
                    }
                }
                if (build.duration != "N/A") {
                    panel {
                        row {
                            if (build.status == CCv2BuildStatus.BUILDING) {
                                label("-").comment("Duration")
                            } else {
                                build.duration.toString().concatWithSpace("minutes")
                                    ?.let { label(it).comment("Duration") }
                            }
                        }
                    }
                }
            }.layout(RowLayout.PARENT_GRID)
        }

        group("Deployment History") {
            row {
                cell(deploymentHistoryPanel).visibleIf(showBuildHistory)
            }

            row {
                panel {
                    row {
                        icon(AnimatedIcon.Default.INSTANCE)
                        label("Retrieving build details...")
                    }
                }.align(Align.CENTER)
            }.visibleIf(showBuildHistory.not())
        }.visible(!isBuildInProgress)
    }.let { Dsl.scrollPanel(it) }


    private fun buildDeploymentHistoryPanel(groupedDeployments: Map<String, List<CCv2DeploymentDto>>) = panel {
        groupedDeployments.forEach {
            val envCode = it.key
            val deployments = it.value

            collapsibleGroup(envCode) {
                deployments.forEach { deployment ->
                    row {
                        panel {
                            row {
                                icon(deployment.status.icon)
                                    .gap(RightGap.SMALL)
                                label(deployment.status.title)
                                    .comment("Status")
                            }
                        }.gap(RightGap.SMALL)

                        panel {
                            row {
                                icon(deployment.updateMode.icon)
                                    .gap(RightGap.SMALL)
                                label(deployment.updateMode.title)
                                    .comment("Platform update mode")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                icon(deployment.strategy.icon)
                                    .gap(RightGap.SMALL)
                                label(deployment.strategy.title)
                                    .comment("Platform deployment mode")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                date("Scheduled date", deployment.scheduledTime)
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                date("Deployed date", deployment.deployedTime)
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                sUser(project, deployment.createdBy, HybrisIcons.CCv2.Deployment.CREATED_BY, "Deployed by")
                            }
                        }
                    }
                }
            }.expanded = true
        }
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -6880893139101434735L
    }

}