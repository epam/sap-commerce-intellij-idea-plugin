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
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2FetchEnvironmentAction
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2ShowServiceDetailsAction
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DataBackupDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.GridBagLayout
import java.awt.datatransfer.StringSelection
import java.io.Serial

class CCv2EnvironmentDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private var environment: CCv2EnvironmentDto
) : SimpleToolWindowPanel(false, true), Disposable {

    private val showBuild = AtomicBooleanProperty(environment.deployedBuild != null)
    private val showServices = AtomicBooleanProperty(environment.services != null)
    private val showDataBackups = AtomicBooleanProperty(environment.dataBackups != null)

    private val buildPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val servicesPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val dataBackupsPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private var rootPanel = rootPanel()

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
            add(CCv2FetchEnvironmentAction(
                subscription,
                environment,
                {
                },
                {
                    environment = it

                    this@CCv2EnvironmentDetailsView.remove(rootPanel)
                    rootPanel = rootPanel()

                    initPanel()
                }
            ))


            actionManager.createActionToolbar("SAP_CX_CCv2_ENVIRONMENT_${System.identityHashCode(environment)}", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initPanel() {
        add(rootPanel)

        initBuildPanel()
        initServicesPanel()
        initDataBackupsPanel()
    }

    private fun initBuildPanel() {
        val deployedBuild = environment.deployedBuild
        if (deployedBuild == null) {
            CCv2Service.getInstance(project).fetchEnvironmentBuild(subscription, environment,
                {
                    showBuild.set(false)
                    environment.deployedBuild = null
                    buildPanel.removeAll()
                },
                { build ->
                    environment.deployedBuild = build

                    invokeLater {
                        showBuild.set(build != null)

                        if (build != null) {
                            buildPanel.add(buildPanel(build))
                        }
                    }
                }
            )
        } else {
            buildPanel.removeAll()
            buildPanel.add(buildPanel(deployedBuild))
        }
    }

    private fun initServicesPanel() {
        val services = environment.services
        if (services == null) {
            CCv2Service.getInstance(project).fetchEnvironmentServices(subscription, environment,
                {
                    showServices.set(false)
                    environment.services = null
                    servicesPanel.removeAll()
                },
                {
                    environment.services = it

                    invokeLater {
                        showServices.set(it != null)

                        if (it != null) {
                            servicesPanel.add(servicesPanel(it))
                        }
                    }
                }
            )
        } else {
            servicesPanel.removeAll()
            servicesPanel.add(servicesPanel(services))
        }
    }

    private fun initDataBackupsPanel() {
        val dataBackups = environment.dataBackups
        if (dataBackups == null) {
            CCv2Service.getInstance(project).fetchEnvironmentDataBackups(subscription, environment,
                {
                    showDataBackups.set(false)
                    environment.dataBackups = null
                    dataBackupsPanel.removeAll()
                },
                {
                    environment.dataBackups = it

                    invokeLater {
                        showDataBackups.set(it != null)

                        if (it != null) {
                            dataBackupsPanel.add(dataBackupsPanel(it))
                        }
                    }
                }
            )
        } else {
            dataBackupsPanel.removeAll()
            dataBackupsPanel.add(dataBackupsPanel(dataBackups))
        }
    }

    private fun buildPanel(build: CCv2BuildDto) = panel {
        row {
            panel {
                row {
                    label(build.name)
                        .bold()
                        .comment("Name")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(HybrisIcons.CCV2_BUILD_BRANCH)
                        .gap(RightGap.SMALL)
                    label(build.branch)
                        .comment("Branch")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(build.code)
                        .comment("Code")
                }
            }

            panel {
                row {
                    label(build.version)
                        .comment("Version")
                }
            }

            panel {
                row {
                    icon(HybrisIcons.CCV2_BUILD_CREATED_BY)
                    label(build.createdBy)
                        .comment("Created by")
                }
            }
        }
            .layout(RowLayout.PARENT_GRID)
    }

    private fun servicesPanel(services: Collection<CCv2ServiceDto>) = panel {
        services.forEach { service ->
            row {
                panel {
                    row {
                        actionButton(
                            CCv2ShowServiceDetailsAction(subscription, environment, service),
                            ActionPlaces.TOOLWINDOW_CONTENT
                        )
                    }
                }.gap(RightGap.SMALL)

                panel {
                    row {
                        browserLink(service.name, service.link)
                            .bold()
                            .comment("Name")
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceModifiedByRow(service)
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceModifiedTimeRow(service)
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceReplicasRow(service)
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2ServiceStatusRow(service)
                }
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    private fun dataBackupsPanel(dataBackups: Collection<CCv2DataBackupDto>) = panel {
        dataBackups.forEach { dataBackup ->
            row {
                panel {
                    row {
                        contextHelp(dataBackup.description)
                        label(dataBackup.name)
                            .comment(dataBackup.dataBackupCode)
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    row {
                        icon(HybrisIcons.CCV2_DATA_BACKUP_CREATED_BY)
                            .gap(RightGap.SMALL)
                        label(dataBackup.createdBy)
                            .comment("Created by")
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    row {
                        label(dataBackup.status)
                            .comment("Status")
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    row {
                        label(dataBackup.buildCode)
                            .comment("Build code")
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    row {
                        label(dataBackup.createdTimestampFormatted)
                            .comment("Created")
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeRestored, "Can be restored")
                }
                    .gap(RightGap.SMALL)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeCanceled, "Can be canceled")
                }
                    .gap(RightGap.SMALL)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeDeleted, "Can be deleted")
                }
            }
                .layout(RowLayout.PARENT_GRID)
        }
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
                    cell(buildPanel)
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
                                    browserLink(mediaStorage.name, mediaStorage.link)
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

            collapsibleGroup("Services") {
                row {
                    cell(servicesPanel)
                }.visibleIf(showServices)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving services...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showServices.not())
            }.expanded = true

            collapsibleGroup("Data Backups") {
                row {
                    cell(dataBackupsPanel)
                }.visibleIf(showDataBackups)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving data backups...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showDataBackups.not())
            }.expanded = true
        }
    }
        .let { Dsl.scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -6880893139101434735L
    }

}