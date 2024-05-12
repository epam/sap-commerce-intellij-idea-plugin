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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
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
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.datatransfer.StringSelection
import java.io.Serial
import javax.swing.JLabel

class CCv2EnvironmentDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private val environment: CCv2Environment
) : SimpleToolWindowPanel(false, true), Disposable {

    private val showBuild = AtomicBooleanProperty(environment.deployedBuild != null)
    private lateinit var buildNameLabel: JLabel
    private lateinit var buildBranchLabel: JLabel
    private lateinit var buildCodeLabel: JLabel
    private lateinit var buildVersionLabel: JLabel
    private lateinit var buildCreatedByLabel: JLabel

    init {
        add(rootPanel())

        if (environment.deployedBuild == null) {
            CCv2Service.getInstance(project).fetchEnvironmentBuild(subscription, environment,
                {
                    showBuild.set(false)
                    environment.deployedBuild = null
                },
                { build ->
                    invokeLater {
                        environment.deployedBuild = build
                        showBuild.set(build != null)

                        if (build != null) {
                            buildNameLabel.text = build.name
                            buildBranchLabel.text = build.branch
                            buildCodeLabel.text = build.code
                            buildVersionLabel.text = build.version
                            buildCreatedByLabel.text = build.createdBy
                        }
                    }
                }
            )
        }
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

                panel {
                    row {
                        icon(HybrisIcons.DYNATRACE)
                            .gap(RightGap.SMALL)
                        browserLink("Dynatrace", environment.dynatraceLink ?: "")
                            .enabled(environment.dynatraceLink != null)
                            .comment(environment.problems
                                ?.let { "problems: <strong>$it</strong>" } ?: "&nbsp;")
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)

                panel {
                    row {
                        icon(HybrisIcons.OPENSEARCH)
                            .gap(RightGap.SMALL)
                        browserLink("OpenSearch", environment.loggingLink ?: "")
                            .enabled(environment.loggingLink != null)
                            .comment("&nbsp;")
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)
            }
                .layout(RowLayout.PARENT_GRID)
                .topGap(TopGap.SMALL)
                .bottomGap(BottomGap.SMALL)

            group("Build") {
                row {
                    val deployedBuild = environment.deployedBuild
                    panel {
                        row {
                            buildNameLabel = label(deployedBuild?.name ?: "")
                                .bold()
                                .comment("Name")
                                .component
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            icon(HybrisIcons.CCV2_BUILD_BRANCH)
                                .gap(RightGap.SMALL)
                            buildBranchLabel = label(deployedBuild?.branch ?: "")
                                .comment("Branch")
                                .component
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            buildCodeLabel = label(deployedBuild?.code ?: "")
                                .comment("Code")
                                .component
                        }
                    }

                    panel {
                        row {
                            buildVersionLabel = label(deployedBuild?.version ?: "")
                                .comment("Version")
                                .component
                        }
                    }

                    panel {
                        row {
                            icon(HybrisIcons.CCV2_BUILD_CREATED_BY)
                            buildCreatedByLabel = label(deployedBuild?.createdBy ?: "")
                                .comment("Created by")
                                .component
                        }
                    }
                }.visibleIf(showBuild)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving build details...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showBuild.not())
            }

            group("Cloud Storage") {
                val mediaStorages = environment.mediaStorages
                if (mediaStorages.isEmpty()) {
                    row {
                        label("No media storages found for environment.")
                            .align(Align.FILL)
                    }
                } else {
                    mediaStorages.forEach { mediaStorage ->
                        row {
                            panel {
                                row {
                                    label(mediaStorage.name)
                                        .bold()
                                        .comment("Name")
                                }
                            }.gap(RightGap.COLUMNS)

                            panel {
                                row {
                                    label(mediaStorage.publicUrl)
                                        .comment("Public URL")
                                }
                            }.gap(RightGap.COLUMNS)

                            panel {
                                row {
                                    link(mediaStorage.code) {
                                        CopyPasteManager.getInstance().setContents(StringSelection(mediaStorage.code))
                                        Notifications.create(NotificationType.INFORMATION, "Account name copied to clipboard", "")
                                            .hideAfter(10)
                                            .notify(project)
                                    }
                                        .comment("Account name")
                                        .applyToComponent {
                                            HelpTooltip()
                                                .setTitle("Click to copy to clipboard")
                                                .installOn(this);
                                        }
                                }
                            }.gap(RightGap.COLUMNS)

                            panel {
                                row {
                                    lateinit var publicKeyActionLink: ActionLink
                                    var retrieved = false
                                    var retrieving = false

                                    publicKeyActionLink = link("Retrieve public key...") {
                                        if (retrieving) return@link

                                        if (retrieved) {
                                            CopyPasteManager.getInstance().setContents(StringSelection(publicKeyActionLink.text))
                                            Notifications.create(NotificationType.INFORMATION, "Public key copied to clipboard", "")
                                                .hideAfter(10)
                                                .notify(project)
                                        } else {
                                            retrieving = true

                                            CCv2Service.getInstance(project).fetchMediaStoragePublicKey(project, subscription, environment, mediaStorage,
                                                {
                                                    publicKeyActionLink.text = "Retrieving..."
                                                },
                                                { publicKey ->
                                                    invokeLater {
                                                        retrieving = false
                                                        publicKeyActionLink.text = "Copy public key"

                                                        if (publicKey != null) {
                                                            retrieved = true
                                                            publicKeyActionLink.text = publicKey

                                                            CopyPasteManager.getInstance().setContents(StringSelection(publicKey))
                                                            Notifications.create(NotificationType.INFORMATION, "Public key copied to clipboard", "")
                                                                .hideAfter(10)
                                                                .notify(project)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                        .comment("Account key")
                                        .applyToComponent {
                                            HelpTooltip()
                                                .setTitle("Click to copy to clipboard")
                                                .installOn(this);
                                        }
                                        .component
                                }
                            }.gap(RightGap.COLUMNS)
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }
            }

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