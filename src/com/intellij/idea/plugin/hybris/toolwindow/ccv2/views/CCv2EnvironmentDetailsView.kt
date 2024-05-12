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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.io.Serial

class CCv2EnvironmentDetailsView(
    private val subscription: CCv2Subscription,
    private val environment: CCv2Environment
) : SimpleToolWindowPanel(false, true), Disposable {

    init {
        add(rootPanel())
    }

    override fun dispose() {
        // NOP
    }

    private fun rootPanel() = panel {
        indent {
            row {
                val environmentCode = if (environment.name != environment.code) "${environment.code} - ${environment.name}"
                else environment.name

                label("$subscription - $environmentCode")
                    .comment("Environment")
                    .bold()
                    .component.also {
                        it.font = JBUI.Fonts.label(26f)
                    }
            }
                .topGap(TopGap.SMALL)
                .bottomGap(BottomGap.SMALL)

            row {
                environment.link
                    ?.let {
                        panel {
                            row {
                                icon(HybrisIcons.EXTENSION_CLOUD)
                                    .gap(RightGap.SMALL)
                                browserLink("Cloud portal", it)
                                    .comment("&nbsp;")
                            }
                        }
                            .gap(RightGap.COLUMNS)
                            .align(AlignY.TOP)
                    }

                panel {
                    row {
                        icon(environment.type.icon)
                            .gap(RightGap.SMALL)
                        label(environment.type.title)
                            .comment("Type")
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)

                panel {
                    row {
                        icon(environment.status.icon)
                        label(environment.status.title)
                            .comment("Status")
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)

//                panel {
//                    row {
//                        icon(AnimatedIcon.Default.INSTANCE)
//                            .comment("Deployment")
//                    }
//                }
            }
                .layout(RowLayout.PARENT_GRID)
                .topGap(TopGap.SMALL)
                .bottomGap(BottomGap.SMALL)

            val deployedBuild = environment.deployedBuild
            if (deployedBuild != null) {
                group("Build") {
                    row {
                        panel {
                            row {
                                label(deployedBuild.name)
                                    .comment("Name")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                icon(HybrisIcons.CCV2_BUILD_BRANCH)
                                    .gap(RightGap.SMALL)
                                label(deployedBuild.branch)
                                    .comment("Branch")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                label(deployedBuild.code)
                                    .comment("Code")
                            }
                        }
                    }
                }
            }

            row {
//                panel {
//                    row {
//                        label("Cloud storage")
//                    }
//                    indent {
//                        row {
//                            label("Hot folders")
//                        }
//                        row {
//                            label("Audit logs")
//                        }
//                        row {
//                            label("Logs")
//                        }
//                    }
//                }
//                    .gap(RightGap.COLUMNS)
//                    .align(AlignY.TOP)
//
//                panel {
//                    row {
//                        label("Data backups")
//                    }
//                    indent {
//                        row {
//                            label("Some backupId")
//                                .comment("Last created:")
//                        }
//
//                        row {
//                            label("Some backupId")
//                                .comment("Last restored:")
//                        }
//
//                        row {
//                            link("Manage") {
//                                // DO something
//                            }
//                        }
//                    }
//                }
//                    .gap(RightGap.COLUMNS)
//                    .align(AlignY.TOP)

                panel {
                    row {
                        label("Monitoring")
                    }
                    indent {
                        row {
                            icon(HybrisIcons.DYNATRACE)
                                .gap(RightGap.SMALL)
                            browserLink("Dynatrace", environment.dynatraceLink ?: "")
                                .enabled(environment.dynatraceLink != null)
                                .comment(environment.problems
                                    ?.let { "problems: <strong>$it</strong>" } ?: "&nbsp;")
                        }

                        row {
                            icon(HybrisIcons.OPENSEARCH)
                                .gap(RightGap.SMALL)
                            browserLink("OpenSearch", environment.loggingLink ?: "")
                                .enabled(environment.loggingLink != null)
                                .comment("&nbsp;")
                        }
                    }
                }.align(AlignY.TOP)
            }.layout(RowLayout.PARENT_GRID)

//            collapsibleGroup("Services") {
//                row {
//                    icon(AnimatedIcon.Default.INSTANCE)
//                        .comment("Loading services...")
//                }
//            }.expanded = true
//
//            collapsibleGroup("Public Endpoints") {
//                row {
//                    icon(AnimatedIcon.Default.INSTANCE)
//                        .comment("Loading public endpoints...")
//                }
//            }.expanded = true
        }
    }
        .let { Dsl.scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -6880893139101434735L
    }

}