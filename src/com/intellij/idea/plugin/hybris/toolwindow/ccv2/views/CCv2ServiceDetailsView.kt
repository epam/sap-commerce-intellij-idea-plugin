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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2.views

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2FetchEnvironmentServiceAction
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2ServiceRestartReplicaAction
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceProperties
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.*
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2ViewUtil
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.invokeLater
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
import java.io.Serial
import javax.swing.JPanel

class CCv2ServiceDetailsView(
    private val project: Project,
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    service: CCv2ServiceDto,
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

    override fun dispose() {
        // NOP
    }

    init {
        initPanel(service)
    }

    private fun installToolbar(service: CCv2ServiceDto) {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(
                CCv2FetchEnvironmentServiceAction(
                    subscription,
                    environment,
                    service,
                    {
                        // hard reset service details on re-fetch
                        it.initialPasswords = null
                        it.customerProperties = null
                        it.greenDeploymentSupported = null

                        initPanel(it)
                    }
                ))
            add(actionManager.getAction("ccv2.reset.cache.action"))

            addSeparator()
            add(actionManager.getAction("ccv2.service.toolbar.actions"))

            actionManager.createActionToolbar("SAP_CX_CCv2_SERVICE_${System.identityHashCode(service)}", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initPanel(service: CCv2ServiceDto) {
        removeAll()

        add(rootPanel(service))
        installToolbar(service)

        initPropertiesPanel(
            service,
            CCv2ServiceProperties.INITIAL_PASSWORDS,
            service.initialPasswords,
            showInitialPasswords,
            initialPasswordsPanel,
            { service.initialPasswords = it },
            { propertiesPanel(it) }
        )

        initPropertiesPanel(
            service,
            CCv2ServiceProperties.CUSTOMER_PROPERTIES,
            service.customerProperties,
            showCustomerProperties,
            customerPropertiesPanel,
            { service.customerProperties = it },
            { propertiesPanel(it) }
        )

        initPropertiesPanel(
            service,
            CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED,
            service.greenDeploymentSupported?.let { mapOf(CCv2ServiceProperties.GREEN_DEPLOYMENT_SUPPORTED_KEY to it.toString()) },
            showGreenDeploymentSupported,
            greenDeploymentSupportedPanel,
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
        service: CCv2ServiceDto,
        serviceProperties: CCv2ServiceProperties,
        currentProperties: Map<String, String>?,
        showFlag: AtomicBooleanProperty,
        container: JPanel,
        onCompleteCallback: (Map<String, String>?) -> Unit,
        panelProvider: (Map<String, String>) -> DialogPanel
    ) {
        if (!service.supportedProperties.contains(serviceProperties)) return

        container.removeAll()

        if (currentProperties != null) {
            container.add(panelProvider.invoke(currentProperties))

            return
        }

        showFlag.set(false)

        CCv2Service.getInstance(project).fetchEnvironmentServiceProperties(
            subscription, environment, service, serviceProperties,
            {
                onCompleteCallback.invoke(it)

                invokeLater {
                    val panel = if (it != null) panelProvider.invoke(it)
                    else CCv2ViewUtil.noDataPanel("No ${serviceProperties.title} found")

                    container.removeAll()
                    container.add(panel)
                    showFlag.set(true)
                }
            }
        )
    }

    private fun propertiesPanel(properties: Map<String, String>) = panel {
        properties.forEach { (key, value) ->
            row {
                panel {
                    row {
                        copyLink(project, null, key, "Key copied to clipboard")
                    }
                }.gap(RightGap.SMALL)

                panel {
                    row {
                        copyLink(project, null, value, "Value copied to clipboard")
                    }
                }
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    private fun rootPanel(service: CCv2ServiceDto) = panel {
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
                panel { ccv2ServiceStatusRow(service) }
                    .gap(RightGap.COLUMNS)

                panel { ccv2ServiceReplicasRow(service) }
                    .gap(RightGap.COLUMNS)

                panel { ccv2ServiceModifiedByRow(service) }
                    .gap(RightGap.COLUMNS)

                panel { ccv2ServiceModifiedTimeRow(service) }

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