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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.help.HybrisWebHelpProvider
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.util.ui.JBUI
import java.util.*
import javax.swing.JLabel

class CCv2DeployBuildDialog(
    private val project: Project,
    private val subscription: CCv2Subscription?,
    private val build: CCv2BuildDto,
) : DialogWrapper(project), Disposable {

    private lateinit var environments: SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>

    private lateinit var subscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var environmentComboBox: ComboBox<CCv2EnvironmentDto>
    private lateinit var modeComboBox: ComboBox<CCv2DeploymentDatabaseUpdateModeEnum>
    private lateinit var strategyComboBox: ComboBox<CCv2DeploymentStrategyEnum>
    private lateinit var fetchingLabel: JLabel
    private lateinit var trackCheckBox: JBCheckBox

    private val environmentModel by lazy { CollectionComboBoxModel<CCv2EnvironmentDto>() }
    private val modeModel by lazy {
        CollectionComboBoxModel(CCv2DeploymentDatabaseUpdateModeEnum.allowedOptions())
    }
    private val strategyModel by lazy {
        CollectionComboBoxModel(CCv2DeploymentStrategyEnum.allowedOptions())
    }

    init {
        title = "Schedule CCv2 Build Deployment"
        super.init()

        val subscriptions = ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions

        isOKActionEnabled = false
        subscriptionComboBox.isEnabled = false
        environmentComboBox.isEnabled = false
        modeComboBox.isEnabled = false
        strategyComboBox.isEnabled = false

        CCv2Service.getInstance(project).fetchEnvironments(
            subscriptions = subscriptions,
            onCompleteCallback = {
                environments = it
                updateEnvironments(subscriptionComboBox.selectedItem as CCv2Subscription)

                subscriptionComboBox.isEnabled = true
                environmentComboBox.isEnabled = true
                modeComboBox.isEnabled = true
                strategyComboBox.isEnabled = true
                fetchingLabel.isVisible = false

                isOKActionEnabled = true
            },
            sendEvents = false
        )
    }

    override fun dispose() = super.dispose()

    override fun createCenterPanel() = panel {
        group("Build Details") {
            row {
                label(build.code)
                    .bold()
                    .label("Code:")
            }.layout(RowLayout.PARENT_GRID)

            row {
                label(build.branch)
                    .bold()
                    .label("Branch:")
                icon(HybrisIcons.CCv2.Build.BRANCH)
            }.layout(RowLayout.PARENT_GRID)
        }

        group("Deployment Details") {
            row {
                subscriptionComboBox = comboBox(
                    CCv2SubscriptionsComboBoxModelFactory.create(project, subscription),
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        label.icon = HybrisIcons.Module.CCV2
                        label.text = value.toString()
                    }
                )
                    .label("Subscription:")
                    .align(AlignX.FILL)
                    .addValidationRule("Please select a subscription for a build.") { it.selectedItem == null }
                    .onChanged {
                        it.selectedItem
                            ?.let { item -> item as? CCv2Subscription }
                            ?.let { subscription -> updateEnvironments(subscription) }
                    }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                environmentComboBox = comboBox(
                    environmentModel,
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        if (value != null) {
                            label.text = value.name
                            label.icon = value.type.icon
                        }
                    }
                )
                    .align(AlignX.FILL)
                    .label("Environment:")
                    .addValidationRule("Please select the environment.") { it.selectedItem == null }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                modeComboBox = comboBox(
                    modeModel,
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        label.text = value.title
                        label.icon = value.icon
                    }
                )
                    .align(AlignX.FILL)
                    .label("Mode:")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                strategyComboBox = comboBox(
                    strategyModel,
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        label.text = value.title
                        label.icon = value.icon
                    }
                )
                    .align(AlignX.FILL)
                    .label("Strategy:")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                trackCheckBox = checkBox("Track deployment progress")
                    .align(AlignX.FILL)
                    .selected(true)
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }

        panel {
            row {
                fetchingLabel = label("Re-fetching environments for all subscriptions...")
                    .component
                    .also { it.icon = AnimatedIcon.Default.INSTANCE }
            }
        }
    }.also {
        it.border = JBUI.Borders.empty(16)
    }

    private fun updateEnvironments(subscription: CCv2Subscription) {
        environmentModel.removeAll()
        environments[subscription]
            ?.toList()
            ?.filter { it.deploymentAllowed }
            ?.let { environmentModel.addAll(0, it) }
        environmentModel.selectedItem = null
    }

    override fun applyFields() {
        val subscription = subscriptionComboBox.selectedItem as CCv2Subscription
        val deploymentRequest = CCv2DeploymentRequest(
            environmentComboBox.selectedItem as CCv2EnvironmentDto,
            modeComboBox.selectedItem as CCv2DeploymentDatabaseUpdateModeEnum,
            strategyComboBox.selectedItem as CCv2DeploymentStrategyEnum,
            true,
            trackCheckBox.isSelected
        )

        CCv2Service.getInstance(project).deployBuild(project, subscription, build, deploymentRequest)
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = subscriptionComboBox
    override fun getHelpId() = HybrisWebHelpProvider.CCV2_DEPLOYMENTS
}