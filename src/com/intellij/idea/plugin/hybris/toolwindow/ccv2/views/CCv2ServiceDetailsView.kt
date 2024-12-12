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
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2FetchEnvironmentServiceAction
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2ServiceRestartReplicaAction
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceProperties
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.ccv2ServiceModifiedByRow
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.ccv2ServiceModifiedTimeRow
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.ccv2ServiceReplicasRow
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.ccv2ServiceStatusRow
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
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.GridBagLayout
import java.awt.datatransfer.StringSelection
import java.io.Serial
import javax.swing.JPanel

class CCv2ServiceDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private var environment: CCv2EnvironmentDto,
    private var service: CCv2ServiceDto,
) : SimpleToolWindowPanel(false, true), Disposable {

    private val showCustomerProperties by lazy { AtomicBooleanProperty(service.customerProperties != null) }
    private val showGreenDeploymentSupported by lazy { AtomicBooleanProperty(service.greenDeploymentSupported != null) }
    private val showInitialPasswords by lazy { AtomicBooleanProperty(service.initialPasswords != null) }

    private val customerPropertiesPanel by lazy {
        JBPanel<JBPanel<*>>(GridBagLayout())
            .also { border = JBUI.Borders.empty() }
    }
    private val initialPasswordsPanel by lazy {
        JBPanel<JBPanel<*>>(GridBagLayout())
            .also { border = JBUI.Borders.empty() }
    }
    private val greenDeploymentSupportedPanel by lazy {
        JBPanel<JBPanel<*>>(GridBagLayout())
            .also { border = JBUI.Borders.empty() }
    }
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

            add(actionManager.getAction("ccv2.service.toolbar.actions"))
            add(CCv2FetchEnvironmentServiceAction(
                subscription,
                environment,
                service,
                {
                },
                {
                    service = it

                    this@CCv2ServiceDetailsView.remove(rootPanel)
                    rootPanel = rootPanel()

                    initPanel()
                }
            ))


            actionManager.createActionToolbar("SAP_CX_CCv2_SERVICE_${System.identityHashCode(service)}", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initPanel() {
        add(rootPanel)

        initPropertiesPanel(
            CCv2ServiceProperties.INITIAL_PASSWORDS,
            service.initialPasswords,
            showInitialPasswords,
            initialPasswordsPanel,
            { service.initialPasswords = null },
            { service.initialPasswords = it },
            { propertiesPanel(it) }
        )

        initPropertiesPanel(
            CCv2ServiceProperties.CUSTOMER_PROPERTIES,
            service.customerProperties,
            showCustomerProperties,
            customerPropertiesPanel,
            { service.customerProperties = null },
            { service.customerProperties = it },
            { propertiesPanel(it) }
        )

        initPropertiesPanel(
            CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED,
            service.greenDeploymentSupported?.let { mapOf(CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED_KEY to it.toString()) },
            showGreenDeploymentSupported,
            greenDeploymentSupportedPanel,
            { service.greenDeploymentSupported = null },
            { service.greenDeploymentSupported = it?.get(CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED_KEY)?.let { value -> value == "true" } },
            { greenDeploymentSupportedPanel(service.greenDeploymentSupported) }
        )
    }

    private fun greenDeploymentSupportedPanel(greenDeploymentSupported: Boolean?) = panel {
        row {
            val text = when (greenDeploymentSupported) {
                true -> "Supported"
                false -> "Not supported"
                null -> "N/A"
            }
            label(text)
                .comment(CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED.title)
                .component.also {
                    when (greenDeploymentSupported) {
                        true -> it.foreground = JBColor.namedColor("hybris.ccv2.greenDeployment.supported", 0x59A869, 0x499C54)
                        false -> it.foreground = JBColor.namedColor("hybris.ccv2.greenDeployment.notSupported", 0xDB5860, 0xC75450)
                        else -> Unit
                    }
                }
        }
    }

    private fun initPropertiesPanel(
        serviceProperties: CCv2ServiceProperties,
        currentProperties: Map<String, String>?,
        showFlag: AtomicBooleanProperty,
        panel: JPanel,
        onStartCallback: () -> Unit,
        onCompleteCallback: (Map<String, String>?) -> Unit,
        panelProvider: (Map<String, String>) -> DialogPanel
    ) {
        if (!service.supportedProperties.contains(serviceProperties)) return

        if (currentProperties != null) {
            panel.removeAll()
            panel.add(panelProvider.invoke(currentProperties))
        } else {
            CCv2Service.getInstance(project).fetchEnvironmentServiceProperties(subscription, environment, service, serviceProperties,
                {
                    showFlag.set(false)
                    onStartCallback.invoke()
                    panel.removeAll()
                },
                {
                    onCompleteCallback.invoke(it)

                    invokeLater {
                        showFlag.set(it != null)

                        if (it != null) {
                            panel.add(panelProvider.invoke(it))
                        }
                    }
                }
            )
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

                panel {
                    ccv2ServiceModifiedTimeRow(service)
                }

                if (service.supportedProperties.contains(CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED)) {
                    panel {
                        row {
                            cell(greenDeploymentSupportedPanel)
                        }.visibleIf(showGreenDeploymentSupported)

                        row {
                            panel {
                                row {
                                    icon(AnimatedIcon.Default.INSTANCE)
                                    label("Checking Green deployment...")
                                }
                            }
                        }.visibleIf(showGreenDeploymentSupported.not())
                    }
                }
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
                                    actionsButton(
                                        actions = listOfNotNull(
                                            CCv2ServiceRestartReplicaAction(subscription, environment, service, replica),
                                        ).toTypedArray(),
                                        ActionPlaces.TOOLWINDOW_CONTENT
                                    )
                                }
                            }.gap(RightGap.SMALL)

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

            if (service.supportedProperties.contains(CCv2ServiceProperties.INITIAL_PASSWORDS)) {
                collapsibleGroup(CCv2ServiceProperties.INITIAL_PASSWORDS.title) {
                    row {
                        cell(initialPasswordsPanel)
                    }.visibleIf(showInitialPasswords)

                    row {
                        panel {
                            row {
                                icon(AnimatedIcon.Default.INSTANCE)
                                label("Retrieving properties...")
                            }
                        }.align(Align.CENTER)
                    }.visibleIf(showInitialPasswords.not())
                }.expanded = true
            }

            if (service.supportedProperties.contains(CCv2ServiceProperties.CUSTOMER_PROPERTIES)) {
                collapsibleGroup(CCv2ServiceProperties.CUSTOMER_PROPERTIES.title) {
                    row {
                        cell(customerPropertiesPanel)
                    }.visibleIf(showCustomerProperties)

                    row {
                        panel {
                            row {
                                icon(AnimatedIcon.Default.INSTANCE)
                                label("Retrieving properties...")
                            }
                        }.align(Align.CENTER)
                    }.visibleIf(showCustomerProperties.not())
                }.expanded = true
            }
        }
    }
        .let { Dsl.scrollPanel(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = 1808556418262990847L
    }

}