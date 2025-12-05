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

package sap.commerce.toolset.ccv2.ui.view

import com.intellij.ide.HelpTooltip
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.event.CCv2EnvironmentsListener
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.*
import sap.commerce.toolset.ui.*
import java.awt.GridBagLayout
import java.awt.datatransfer.StringSelection
import java.io.Serial

class CCv2EnvironmentDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto
) : SimpleToolWindowPanel(false, true), Disposable {

    private val showBuild = AtomicBooleanProperty(environment.deployedBuild != null)
    private val showServices = AtomicBooleanProperty(environment.services != null)
    private val showEndpoints = AtomicBooleanProperty(environment.endpoints != null)
    private val showDataBackups = AtomicBooleanProperty(environment.dataBackups != null)
    private val showScaling = AtomicBooleanProperty(environment.scaling != null)

    private val buildPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val servicesPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val endpointsPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val dataBackupsPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }
    private val scalingPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also { border = JBUI.Borders.empty() }

    override fun dispose() {
        // NOP
    }

    init {
        subscribe()

        initPanel(environment)
    }

    private fun subscribe() = with(project.messageBus.connect(this)) {
        subscribe(CCv2EnvironmentsListener.TOPIC, object : CCv2EnvironmentsListener {
            override fun onEndpointUpdate(data: CCv2EnvironmentDto) {
                if (environment.link != data.link) return
                initEndpointsPanel(data)
            }

            override fun onScalingFetched(environment: CCv2EnvironmentDto, data: CCv2EnvironmentScalingDto?) {
                if (this@CCv2EnvironmentDetailsView.environment.link != environment.link) return
                val panel = if (data != null) scalingPanel(data)
                else CCv2ToolWindowUtil.noDataPanel("No cluster details found for the given environment.")

                scalingPanel.removeAll()
                scalingPanel.add(panel)
                showScaling.set(true)
            }

            override fun onScalingFetchingError(environment: CCv2EnvironmentDto, e: Throwable) {
                if (this@CCv2EnvironmentDetailsView.environment.link != environment.link) return
                val panel = CCv2ToolWindowUtil.noDataPanel("Insufficient permissions to view scaling details", EditorNotificationPanel.Status.Warning)

                scalingPanel.removeAll()
                scalingPanel.add(panel)
                showScaling.set(true)
            }
        }
        )
    }

    override fun uiDataSnapshot(sink: DataSink) {
        super.uiDataSnapshot(sink)

        sink[CCv2UiConstants.DataKeys.Subscription] = subscription
        sink[CCv2UiConstants.DataKeys.Environment] = environment
        sink[CCv2UiConstants.DataKeys.EnvironmentFetchCallback] = {
            // hard reset env details on re-fetch
            it.scaling = null
            it.endpoints = null
            it.services = null
            it.dataBackups = null
            it.deployedBuild = null

            initPanel(it)
        }
    }

    private fun installToolbar(environment: CCv2EnvironmentDto) {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(actionManager.getAction("ccv2.environment.fetch.action"))
            add(actionManager.getAction("ccv2.reset.cache.action"))
            addSeparator()

            add(actionManager.getAction("ccv2.environment.toolbar.actions"))

            actionManager.createActionToolbar("SAP_CX_CCv2_ENVIRONMENT_${System.identityHashCode(environment)}", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initPanel(environment: CCv2EnvironmentDto) {
        removeAll()

        add(rootPanel(environment))
        installToolbar(environment)

        initScalingPanel(environment)
        initBuildPanel(environment)
        initEndpointsPanel(environment)
        initServicesPanel(environment)
        initDataBackupsPanel(environment)
    }

    private fun initScalingPanel(environment: CCv2EnvironmentDto) {
        scalingPanel.removeAll()
        showScaling.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentScaling(subscription, environment)
    }

    private fun initBuildPanel(environment: CCv2EnvironmentDto) {
        val deployedBuild = environment.deployedBuild
        if (deployedBuild != null) {
            buildPanel.removeAll()
            buildPanel.add(buildPanel(deployedBuild))
            return
        }

        showBuild.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentBuild(
            subscription, environment,
            { build ->
                environment.deployedBuild = build

                invokeLater {
                    val panel = if (build != null) buildPanel(build)
                    else CCv2ToolWindowUtil.noDataPanel("No build found")

                    buildPanel.removeAll()
                    buildPanel.add(panel)
                    showBuild.set(true)
                }
            }
        )
    }

    private fun initServicesPanel(environment: CCv2EnvironmentDto) {
        val services = environment.services
        if (services != null) {
            servicesPanel.removeAll()
            servicesPanel.add(servicesPanel(environment, services))
            return
        }

        showServices.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentServices(
            subscription, environment,
            {
                environment.services = it

                invokeLater {
                    val panel = if (it != null) servicesPanel(environment, it)
                    else CCv2ToolWindowUtil.noDataPanel("No services found")

                    servicesPanel.removeAll()
                    servicesPanel.add(panel)
                    showServices.set(true)
                }
            }
        )
    }

    private fun initEndpointsPanel(environment: CCv2EnvironmentDto) {
        val endpoints = environment.endpoints
        if (endpoints != null) {
            endpointsPanel.removeAll()
            endpointsPanel.add(endpointsPanel(endpoints))
            return
        }

        showEndpoints.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentEndpoints(
            subscription, environment,
            {
                environment.endpoints = it

                invokeLater {
                    val panel = if (it != null) endpointsPanel(it)
                    else CCv2ToolWindowUtil.noDataPanel("No public endpoints found")

                    endpointsPanel.removeAll()
                    endpointsPanel.add(panel)
                    showEndpoints.set(true)
                }
            }
        )
    }

    private fun initDataBackupsPanel(environment: CCv2EnvironmentDto) {
        val dataBackups = environment.dataBackups
        if (dataBackups != null) {
            dataBackupsPanel.removeAll()
            dataBackupsPanel.add(dataBackupsPanel(dataBackups))
            return
        }

        showDataBackups.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentDataBackups(
            subscription, environment,
            {
                environment.dataBackups = it

                invokeLater {
                    val panel = if (it != null) dataBackupsPanel(it)
                    else CCv2ToolWindowUtil.noDataPanel("No data backups found")

                    dataBackupsPanel.removeAll()
                    dataBackupsPanel.add(panel)
                    showDataBackups.set(true)
                }
            }
        )
    }

    private fun scalingPanel(data: CCv2EnvironmentScalingDto) = panel {
        // cluster will always contain at least one cluster and db schema
        val cluster = data.kubernetesClusters.first()
        val dbSchema = data.databaseSchemas.first()
        row {
            icon(HybrisIcons.CCv2.Environment.CLUSTER)
                .gap(RightGap.SMALL)
            label(cluster.workerName)
                .comment("Cluster")
                .gap(RightGap.COLUMNS)
                .align(AlignY.TOP)

            icon(HybrisIcons.CCv2.Environment.DATABASE_SCHEMA)
                .gap(RightGap.SMALL)
            label(dbSchema.performanceName)
                .comment("Database")
                .gap(RightGap.COLUMNS)
                .align(AlignY.TOP)

            icon(HybrisIcons.CCv2.Environment.DATABASE_SIZE)
                .gap(RightGap.SMALL)
            label(StringUtil.formatFileSize(dbSchema.maxSizeInMb * 1024 * 1024))
                .comment("Max size")
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
                    icon(HybrisIcons.CCv2.Build.REVISION).gap(RightGap.SMALL)
                    copyLink(project, "Revision", build.revision, "Build Revision copied to clipboard")
                }
            }.gap(RightGap.SMALL)

            panel {
                row {
                    icon(HybrisIcons.CCv2.Build.BRANCH).gap(RightGap.SMALL)
                    copyLink(project, "Branch", build.branch, "Build Branch copied to clipboard")
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
                    sUser(project, build.createdBy, HybrisIcons.CCv2.Build.CREATED_BY)
                }
            }
        }
            .layout(RowLayout.PARENT_GRID)
    }

    private fun endpointsPanel(endpoints: Collection<CCv2EndpointDto>) = panel {
        endpoints
            .sortedWith(compareBy<CCv2EndpointDto> { it.service }
                .thenBy { it.name })
            .forEach { endpoint ->
                row {
                    panel {
                        row {
                            val actionManager = ActionManager.getInstance()
                            actionsButton(
                                actionManager.getAction("ccv2.endpoint.toggleMaintenanceMode.action"),
                                actionManager.getAction("ccv2.endpoint.delete.action")
                            ) {
                                it[CCv2UiConstants.DataKeys.Subscription] = subscription
                                it[CCv2UiConstants.DataKeys.Environment] = environment
                                it[CCv2UiConstants.DataKeys.Endpoint] = endpoint
                            }
                        }
                    }.gap(RightGap.SMALL)

                    panel {
                        row {
                            browserLink(endpoint.name, endpoint.link)
                                .bold()
                                .comment("Name")
                            if (endpoint.maintenanceMode) contextHelp(
                                HybrisIcons.CCv2.Endpoint.MAINTENANCE_MODE,
                                "Maintenance mode active"
                            )
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            label(endpoint.webProxy.title)
                                .comment("Web proxy")
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            label(endpoint.service)
                                .comment("Service")
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            val url = if (endpoint.url.startsWith("http")) endpoint.url
                            else "https://${endpoint.url}"
                            val icon = if (url.startsWith("https")) HybrisIcons.CCv2.Endpoint.SECURE
                            else HybrisIcons.CCv2.Endpoint.UNSECURE

                            icon(icon)
                                .gap(RightGap.SMALL)
                            browserLink(endpoint.url, url)
                                .comment("URL")
                        }
                    }.gap(RightGap.COLUMNS)
                }.layout(RowLayout.PARENT_GRID)
            }
    }

    private fun servicesPanel(environment: CCv2EnvironmentDto, services: Collection<CCv2ServiceDto>) = panel {
        services.forEach { service ->
            row {
                panel {
                    row {
                        actionButton(
                            ActionManager.getInstance().getAction("ccv2.service.showDetails.action"),
                            ActionPlaces.TOOLWINDOW_CONTENT
                        ) {
                            it[CCv2UiConstants.DataKeys.Subscription] = subscription
                            it[CCv2UiConstants.DataKeys.Environment] = environment
                            it[CCv2UiConstants.DataKeys.Service] = service
                        }
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
                    row {
                        sUser(project, service.modifiedBy, HybrisIcons.CCv2.Service.MODIFIED_BY, "Modified by")
                    }
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
                        contextHelp(HybrisIcons.CCv2.BACKUPS, dataBackup.description).gap(RightGap.SMALL)
                        label(dataBackup.name)
                            .comment(dataBackup.dataBackupCode)
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    row {
                        sUser(project, dataBackup.createdBy, HybrisIcons.CCv2.Environment.DATA_BACKUP_CREATED_BY)
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
                        date("Created", dataBackup.createdTimestamp)
                    }
                }
                    .gap(RightGap.COLUMNS)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeRestored, "Restorable")
                }
                    .gap(RightGap.SMALL)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeCanceled, "Cancelable")
                }
                    .gap(RightGap.SMALL)

                panel {
                    ccv2StatusYesNo(dataBackup.canBeDeleted, "Deletable")
                }
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    private fun rootPanel(environment: CCv2EnvironmentDto) = panel {
        indent {
            row {
                val environmentCode = if (environment.name != environment.code) "${environment.code} - ${environment.name}"
                else environment.name

                label("${subscription.presentableName} - $environmentCode")
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
                                icon(HybrisIcons.Extension.CLOUD)
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
                            .gap(RightGap.SMALL)
                        label(environment.status.title)
                            .comment("Status")
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)

                panel {
                    row {
                        dynatrace(environment)
                    }
                }
                    .gap(RightGap.COLUMNS)
                    .align(AlignY.TOP)

                panel {
                    row {
                        icon(HybrisIcons.CCv2.OPENSEARCH)
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

            group("Cluster") {
                row {
                    cell(scalingPanel)
                }.visibleIf(showScaling)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving scaling details...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showScaling.not())
            }

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

            collapsibleGroup("Public Endpoints") {
                row {
                    cell(endpointsPanel)
                }.visibleIf(showEndpoints)

                row {
                    panel {
                        row {
                            icon(AnimatedIcon.Default.INSTANCE)
                            label("Retrieving public endpoints...")
                        }
                    }.align(Align.CENTER)
                }.visibleIf(showEndpoints.not())
            }.expanded = true

            collapsibleGroup("Cloud Storage") {
                val mediaStorages = environment.mediaStorages
                if (mediaStorages.isEmpty()) {
                    inlineBanner("No media storages found for environment.")
                } else {
                    mediaStorages.forEach { mediaStorage ->
                        row {
                            panel {
                                row {
                                    icon(HybrisIcons.CCv2.Environment.BLOB_STORAGE)
                                        .gap(RightGap.SMALL)
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
                                    copyLink(project, "Account name", mediaStorage.code, "Account name copied to clipboard")
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

                                            CCv2Service.getInstance(project).fetchMediaStoragePublicKey(
                                                project, subscription, environment, mediaStorage,
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
                                                .installOn(this)
                                        }
                                        .component
                                }
                            }.gap(RightGap.COLUMNS)
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }
            }
                .expanded = true

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
        .let { scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -6880893139101434735L
    }

}