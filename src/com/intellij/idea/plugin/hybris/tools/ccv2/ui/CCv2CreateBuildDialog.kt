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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.help.HybrisWebHelpProvider
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import com.intellij.util.ui.JBUI
import java.util.*
import javax.swing.Icon

class CCv2CreateBuildDialog(
    private val project: Project,
    private val subscription: CCv2Subscription?,
    private val build: CCv2BuildDto?,
) : DialogWrapper(project) {

    private class DeploymentOption(
        val environment: CCv2EnvironmentDto,
        val modeComboBox: ComboBox<CCv2DeploymentDatabaseUpdateModeEnum>,
        val strategyComboBox: ComboBox<CCv2DeploymentStrategyEnum>,
        val deployCheckBox: JBCheckBox
    ) {
        fun toRequest(track: Boolean) = CCv2DeploymentRequest(
            environment,
            modeComboBox.selectedItem as CCv2DeploymentDatabaseUpdateModeEnum,
            strategyComboBox.selectedItem as CCv2DeploymentStrategyEnum,
            deployCheckBox.isSelected,
            track
        )
    }

    private val deploymentOptions = mutableListOf<DeploymentOption>()

    private lateinit var subscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var nameTextField: JBTextField
    private lateinit var branchTextField: JBTextField
    private lateinit var trackBuildCheckBox: JBCheckBox
    private lateinit var trackDeploymentCheckBox: JBCheckBox
    private lateinit var autoDeploymentPlaceholder: Placeholder

    init {
        title = "Schedule New CCv2 Build"
        super.init()

        refreshAutoDeploymentPanel(subscription)
    }

    override fun createCenterPanel() = panel {
        group("Build Details") {
            row {
                subscriptionComboBox = comboBox(
                    CCv2SubscriptionsComboBoxModelFactory.create(project, subscription),
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        if (value != null) {
                            label.icon = HybrisIcons.Module.CCV2
                            label.text = value.toString()
                        }
                    }
                )
                    .label("Subscription:")
                    .align(AlignX.FILL)
                    .gap(RightGap.SMALL)
                    .addValidationRule("Please select a subscription for a build.") { it.selectedItem == null }
                    .onChanged { refreshAutoDeploymentPanel(it.selectedItem as CCv2Subscription) }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                nameTextField = textField()
                    .label("Name:")
                    .align(AlignX.FILL)
                    .addValidationRule("Please specify name for a build.") { it.text.isBlank() }
                    .component
                    .also { it.text = build?.name }
            }.layout(RowLayout.PARENT_GRID)

            row {
                branchTextField = textField()
                    .label("Branch or Tag:")
                    .align(AlignX.FILL)
                    .gap(RightGap.SMALL)
                    .addValidationRule("Please specify a branch for a build.") { it.text.isBlank() }
                    .component
                    .also { it.text = build?.branch }
            }.layout(RowLayout.PARENT_GRID)
        }

        group("Auto-Deployment") {
            row {
                autoDeploymentPlaceholder = placeholder().align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)
        }

        group("Tracking") {
            row {
                trackBuildCheckBox = checkBox("Track build progress")
                    .align(AlignX.FILL)
                    .comment("Mandatory for auto-deployment")
                    .selected(true)
                    .component
            }

            row {
                trackDeploymentCheckBox = checkBox("Track deployment progress")
                    .align(AlignX.FILL)
                    .selected(true)
                    .component
            }
        }
    }.also {
        it.border = JBUI.Borders.empty(16)
    }

    override fun applyFields() {
        val subscription = subscriptionComboBox.selectedItem as CCv2Subscription
        val name = nameTextField.text!!
        val branch = branchTextField.text!!
        val trackBuild = trackBuildCheckBox.isSelected
        val trackDeployment = trackDeploymentCheckBox.isSelected
        val deploymentRequests = deploymentOptions
            .filter { it.deployCheckBox.isSelected }
            .map { it.toRequest(trackDeployment) }
        deploymentOptions.clear()

        val buildRequest = CCv2BuildRequest(subscription, name, branch, trackBuild, deploymentRequests)

        CCv2Service.getInstance(project).createBuild(buildRequest)
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = subscriptionComboBox
    override fun getHelpId() = HybrisWebHelpProvider.CCV2_DEPLOYMENTS

    private fun refreshAutoDeploymentPanel(subscription: CCv2Subscription?) {
        deploymentOptions.clear()

        if (subscription == null) {
            autoDeploymentPlaceholder.component = infoPanel(
                "Please, select a Subscription first...",
                HybrisIcons.Module.CCV2
            )

            return
        }
        CCv2Service.getInstance(project).fetchEnvironments(
            subscriptions = listOf(subscription),
            onStartCallback = {
                autoDeploymentPlaceholder.component = infoPanel(
                    "Re-fetching environments for the subscription...",
                    AnimatedIcon.Default.INSTANCE
                )
            },
            onCompleteCallback = { environments ->
                val environmentsPanel = getEnvironmentsPanel(environments, subscription)

                autoDeploymentPlaceholder.component = environmentsPanel
            },
            sendEvents = false
        )
    }

    private fun getEnvironmentsPanel(
        environmentPerSubscription: SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>,
        subscription: CCv2Subscription
    ): DialogPanel = environmentPerSubscription[subscription]
        ?.filter { it.link != null }
        ?.takeIf { it.isNotEmpty() }
        ?.sortedWith(compareBy({ it.type }, { it.name }))
        ?.let { environments ->
            panel {
                environments.forEach { environment ->
                    var modeComboBox: ComboBox<CCv2DeploymentDatabaseUpdateModeEnum>? = null
                    var strategyComboBox: ComboBox<CCv2DeploymentStrategyEnum>? = null
                    var deployCheckBox: JBCheckBox? = null
                    row {
                        panel {
                            row {
                                icon(environment.type.icon).gap(RightGap.SMALL)
                                browserLink(environment.name, environment.link!!)
                                    .comment(environment.code + " | " + environment.type.title)
                                    .bold()
                            }
                        }.gap(RightGap.SMALL)

                        panel {
                            row {
                                deployCheckBox = checkBox("Deploy")
                                    .align(AlignX.FILL)
                                    .gap(RightGap.SMALL)
                                    .comment(environment.deploymentStatus.title)
                                    .enabled(environment.canDeploy())
                                    .component
                                icon(environment.deploymentStatus.icon)
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                modeComboBox = comboBox(
                                    CollectionComboBoxModel(CCv2DeploymentDatabaseUpdateModeEnum.allowedOptions()),
                                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                                        label.text = value.title
                                        label.icon = value.icon
                                    }
                                )
                                    .enabledIf(deployCheckBox!!.selected)
                                    .comment("Mode")
                                    .component
                            }
                        }.gap(RightGap.SMALL)

                        panel {
                            row {
                                strategyComboBox = comboBox(
                                    CollectionComboBoxModel(CCv2DeploymentStrategyEnum.allowedOptions()),
                                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                                        label.text = value.title
                                        label.icon = value.icon
                                    }
                                )
                                    .enabledIf(deployCheckBox!!.selected)
                                    .comment("Strategy")
                                    .component
                            }
                        }.gap(RightGap.SMALL)
                    }.layout(RowLayout.PARENT_GRID)

                    deploymentOptions.add(DeploymentOption(environment, modeComboBox!!, strategyComboBox!!, deployCheckBox!!))
                }
            }
        }
        ?: infoPanel(
            "No environments ready for deployment...",
            AllIcons.General.Warning,
        )

    private fun infoPanel(message: String, icon: Icon): DialogPanel = panel {
        row {
            label(message)
                .component
                .also { it.icon = icon }
        }
    }
}