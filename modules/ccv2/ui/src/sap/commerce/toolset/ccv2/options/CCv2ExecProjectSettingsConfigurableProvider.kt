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

package sap.commerce.toolset.ccv2.options

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionListPanel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.isHybrisProject

class CCv2ExecProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "CCv2", "[y] SAP Commerce Cloud CCv2 configuration."
    ) {

        private lateinit var timeoutTextField: JBTextField
        private lateinit var defaultCCv2TokenTextField: JBPasswordField
        private lateinit var activeCCv2SubscriptionComboBox: ComboBox<CCv2Subscription>
        private lateinit var subscriptionListPanel: CCv2SubscriptionListPanel
        private lateinit var subscriptionsComboBoxModel: CCv2SubscriptionsComboBoxModel

        private val editable = AtomicBooleanProperty(false)
        private val projectSettings = CCv2ProjectSettings.getInstance()
        private val developerSettings = CCv2DeveloperSettings.getInstance(project)
        private val mutable = projectSettings.state.mutable()
        private var originalToken: String? = null
        private var originalSubscriptions = mutable.subscriptions
        private var originalActiveSubscription = developerSettings.getActiveCCv2Subscription()
        private var originalTimeout = projectSettings.readTimeout

        override fun createPanel(): DialogPanel {
            // disposable is being created only now, do not move dependant items
            subscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true, disposable = disposable)
            subscriptionListPanel = CCv2SubscriptionListPanel(disposable) {
                val previousSelectedItem = subscriptionsComboBoxModel.selectedItem?.asSafely<CCv2Subscription>()?.uuid
                val modifiedSubscriptions = subscriptionListPanel.data.map { it.immutable() }
                subscriptionsComboBoxModel.refresh(modifiedSubscriptions)
                subscriptionsComboBoxModel.selectedItem = modifiedSubscriptions.find { it.uuid == previousSelectedItem }
                activeCCv2SubscriptionComboBox.repaint()
            }

            return panel {
                row {
                    activeCCv2SubscriptionComboBox = comboBox(
                        subscriptionsComboBoxModel,
                        renderer = SimpleListCellRenderer.create { label, value, _ ->
                            if (value != null) {
                                label.icon = HybrisIcons.Module.CCV2
                                label.text = value.presentableName
                            } else {
                                label.text = "-- all subscriptions --"
                            }
                        }
                    )
                        .onIsModified { originalActiveSubscription?.uuid != activeCCv2SubscriptionComboBox.selectedItem?.asSafely<CCv2Subscription>()?.uuid }
                        .label("Subscription:")
                        .enabledIf(editable)
                        .comment("Subscriptions are IntelliJ IDEA application-aware and can be changes via corresponding settings: [y] SAP CX > CCv2.")
                        .component
                }

                separator()

                row {
                    label("CCv2 token:")
                    defaultCCv2TokenTextField = passwordField()
                        .comment(
                            """
                                            Specify developer specific Token for CCv2 API, it will be stored in the OS specific secure storage under <strong>SAP CX CCv2 Token</strong> alias.<br>
                                            Official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html">help.sap.com - Generating API Tokens</a>.
                                        """.trimIndent()
                        )
                        .align(AlignX.FILL)
                        .enabledIf(editable)
                        .onIsModified { (originalToken ?: "") != String(defaultCCv2TokenTextField.password) }
                        .component
                }.layout(RowLayout.PARENT_GRID)

                row {
                    label("Read timeout:")
                    timeoutTextField = intTextField(10..Int.MAX_VALUE)
                        .comment(
                            """
                                            Indicates read timeout in seconds when invoking Cloud Portal API.
                                        """.trimIndent()
                        )
                        .bindIntText(mutable::readTimeout)
                        .enabledIf(editable)
                        .onIsModified { originalTimeout != mutable.readTimeout }
                        .component
                }

                group("Subscriptions", false) {
                    row {
                        cell(subscriptionListPanel)
                            .onIsModified { subscriptionListPanel.data != originalSubscriptions }
                            .align(AlignX.FILL)
                    }
                }
            }
        }

        override fun reset() {
            subscriptionsComboBoxModel.refresh(originalSubscriptions.map { it.immutable() })
            subscriptionListPanel.data = originalSubscriptions.map { it.copy() }
            timeoutTextField.text = originalTimeout.toString()
            activeCCv2SubscriptionComboBox.selectedItem = originalActiveSubscription
            defaultCCv2TokenTextField.text = originalToken

            initForm()
        }

        override fun apply() {
            super.apply()

            developerSettings.activeCCv2SubscriptionID = activeCCv2SubscriptionComboBox.selectedItem?.asSafely<CCv2Subscription>()?.uuid
            projectSettings.readTimeout = timeoutTextField.text.toIntOrNull() ?: 60
            projectSettings.subscriptions = subscriptionListPanel.data.map { it.immutable() }

            val token = String(defaultCCv2TokenTextField.password).takeIf { it.isNotBlank() }
            originalToken = token
            projectSettings.saveDefaultCCv2Token(token)

            subscriptionListPanel.data.forEach {
                projectSettings.saveCCv2Token(it.uuid, it.ccv2Token)
            }

            originalActiveSubscription = developerSettings.getActiveCCv2Subscription()
            originalTimeout = projectSettings.readTimeout
            originalSubscriptions.clear()
            originalSubscriptions.addAll(subscriptionListPanel.data)
        }

        private fun initForm() {
            var expectedLoads = 1 + originalSubscriptions.size

            projectSettings.loadDefaultCCv2Token {
                val ccv2Token = projectSettings.getCCv2Token()

                originalToken = ccv2Token
                defaultCCv2TokenTextField.text = originalToken

                expectedLoads--
                if (expectedLoads == 0) {
                    editable.set(true)
                }
            }

            originalSubscriptions.forEach { subscription ->
                projectSettings.loadCCv2Token(subscription.uuid) { ccv2Token ->
                    subscription.ccv2Token = ccv2Token
                    subscriptionListPanel.data
                        .find { it.uuid == subscription.uuid }
                        ?.ccv2Token = ccv2Token

                    expectedLoads--
                    if (expectedLoads == 0) {
                        editable.set(true)
                    }
                }
            }
        }
    }
}