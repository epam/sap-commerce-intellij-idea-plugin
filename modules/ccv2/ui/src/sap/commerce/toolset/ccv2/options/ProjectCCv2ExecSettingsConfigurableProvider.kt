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

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionListPanel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.equalsIgnoreOrder
import sap.commerce.toolset.isHybrisProject

class ProjectCCv2ExecSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "CCv2", "[y] SAP Commerce Cloud CCv2 configuration."
    ) {

        private var originalCCv2Token: String? = null
        private lateinit var defaultCCv2TokenTextField: JBPasswordField
        private lateinit var activeCCv2SubscriptionComboBox: ComboBox<CCv2Subscription>

        private val originalSettingsState = CCv2ProjectSettings.getInstance().state
        private val mutableCCv2ProjectSettingsState = originalSettingsState.mutable()
        private val ccv2SubscriptionListPanel = CCv2SubscriptionListPanel()
        private lateinit var ccv2SubscriptionsModel: CCv2SubscriptionsComboBoxModel

        init {
            reset()
        }

        override fun createPanel(): DialogPanel {
            ccv2SubscriptionsModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true, disposable = disposable)

            return panel {
                row {
                    activeCCv2SubscriptionComboBox = comboBox(
                        ccv2SubscriptionsModel,
                        renderer = SimpleListCellRenderer.create { label, value, _ ->
                            if (value != null) {
                                label.icon = HybrisIcons.Module.CCV2
                                label.text = value.toString()
                            } else {
                                label.text = "-- all subscriptions --"
                            }
                        }
                    )
                        .label("Subscription:")
                        .comment("Subscriptions are IntelliJ IDEA application-aware and can be changes via corresponding settings: [y] SAP CX > CCv2.")
                        .component
                }.layout(RowLayout.PARENT_GRID)

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
                        .component
                }.layout(RowLayout.PARENT_GRID)

                row {
                    label("Read timeout:")
                    intTextField(10..Int.MAX_VALUE)
                        .comment(
                            """
                                    Indicates read timeout in seconds when invoking Cloud Portal API.
                                """.trimIndent()
                        )
                        .bindIntText(mutableCCv2ProjectSettingsState::readTimeout)
                }

                group("Subscriptions", false) {
                    row {
                        cell(ccv2SubscriptionListPanel)
                            .align(AlignX.FILL)
                    }
                }
            }
        }

        override fun isModified(): Boolean {
            return originalCCv2Token != String(defaultCCv2TokenTextField.password)
                || activeCCv2SubscriptionComboBox.selectedItem != CCv2DeveloperSettings.getInstance(project).getActiveCCv2Subscription()
                || ccv2SubscriptionListPanel.data.equalsIgnoreOrder(CCv2ProjectSettings.getInstance().mutable().subscriptions).not()
        }

        override fun reset() {
            loadOriginalCCv2Token()

            ccv2SubscriptionsModel.refresh()
            activeCCv2SubscriptionComboBox.selectedItem = CCv2DeveloperSettings.getInstance(project).getActiveCCv2Subscription()

            ccv2SubscriptionListPanel.data = CCv2ProjectSettings.getInstance().mutable().subscriptions
            ccv2SubscriptionListPanel.data.forEach { subscription ->
                CCv2ProjectSettings.getInstance().loadCCv2Token(subscription.uuid) {
                    subscription.ccv2Token = it
                }
            }
        }

        override fun apply() {
            super.apply()

            val ccv2ProjectSettings = CCv2ProjectSettings.getInstance()

            mutableCCv2ProjectSettingsState.subscriptions.forEach {
                ccv2ProjectSettings.saveCCv2Token(it.uuid, it.ccv2Token)
            }

            with (mutableCCv2ProjectSettingsState.immutable()) {
                ccv2ProjectSettings.readTimeout = readTimeout
                ccv2ProjectSettings.subscriptions = subscriptions
            }

            ccv2ProjectSettings.saveDefaultCCv2Token(String(defaultCCv2TokenTextField.password)) {}
        }

        private fun loadOriginalCCv2Token() {
            defaultCCv2TokenTextField.isEnabled = false

            val settings = CCv2ProjectSettings.getInstance()

            settings.loadDefaultCCv2Token {
                val ccv2Token = settings.getCCv2Token()

                originalCCv2Token = ccv2Token
                defaultCCv2TokenTextField.text = ccv2Token
                defaultCCv2TokenTextField.isEnabled = true
            }
        }
    }

    override fun dispose() = Unit
}