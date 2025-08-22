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

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SubscriptionDto
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionListPanel
import sap.commerce.toolset.equalsIgnoreOrder

class ApplicationCCv2SettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "CCv2", "[y] SAP Commerce Cloud CCv2 configuration."
    ) {

        private val ccv2ProjectSettings = CCv2ProjectSettings.getInstance()
        private var originalCCv2Token: String? = ""
        private var originalCCv2Subscriptions = ccv2ProjectSettings.ccv2Subscriptions
            .map { it.toDto() }

        private lateinit var defaultCCv2TokenTextField: JBPasswordField
        private val ccv2SubscriptionListPanel = CCv2SubscriptionListPanel(
            ccv2ProjectSettings.ccv2Subscriptions
                .map { it.toDto() }
        )

        init {
            loadCCv2TokensForSubscriptions(originalCCv2Subscriptions)
            loadCCv2TokensForSubscriptions(ccv2SubscriptionListPanel.data)
        }

        override fun createPanel() = panel {
            row {
                label("CCv2 token:")
                defaultCCv2TokenTextField = passwordField()
                    .comment(
                        """
                            Specify developer specific Token for CCv2 API, it will be stored in the OS specific secure storage under <strong>SAP CX CCv2 Token</strong> alias.<br>
                            Official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html">help.sap.com - Generating API Tokens</a>.
                        """.trimIndent()
                    )
                    .align(AlignX.RIGHT)
                    .onIsModified { originalCCv2Token != String(defaultCCv2TokenTextField.password) }
                    .onReset {
                        defaultCCv2TokenTextField.isEnabled = false

                        ccv2ProjectSettings.loadDefaultCCv2Token {
                            val ccv2Token = ccv2ProjectSettings.getCCv2Token()
                            originalCCv2Token = ccv2Token

                            defaultCCv2TokenTextField.text = ccv2Token
                            defaultCCv2TokenTextField.isEnabled = true
                        }
                    }
                    .onApply {
                        ccv2ProjectSettings.saveDefaultCCv2Token(String(defaultCCv2TokenTextField.password)) {
                            originalCCv2Token = it
                        }
                    }
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
                    .bindIntText(ccv2ProjectSettings::ccv2ReadTimeout)
            }

            group("CCv2 Subscriptions", false) {
                row {
                    cell(ccv2SubscriptionListPanel)
                        .align(AlignX.FILL)
                        .onApply {
                            val subscriptions = ccv2SubscriptionListPanel.data
                            subscriptions.forEach {
                                ccv2ProjectSettings.saveCCv2Token(it.uuid, it.ccv2Token)
                            }
                            originalCCv2Subscriptions = subscriptions
                            ccv2ProjectSettings.setCCv2Subscriptions(subscriptions)

                            ccv2SubscriptionListPanel.data = originalCCv2Subscriptions
                                .map { it.copy() }
                        }
                        .onReset {
                            ccv2SubscriptionListPanel.data = ccv2ProjectSettings.ccv2Subscriptions
                                .map { it.toDto() }

                            loadCCv2TokensForSubscriptions(ccv2SubscriptionListPanel.data)
                        }
                        .onIsModified { ccv2SubscriptionListPanel.data.equalsIgnoreOrder(originalCCv2Subscriptions).not() }
                }
            }
        }

        private fun loadCCv2TokensForSubscriptions(subscriptions: List<CCv2SubscriptionDto>) {
            subscriptions.forEach { subscription ->
                ccv2ProjectSettings.loadCCv2Token(subscription.uuid) {
                    subscription.ccv2Token = it
                }
            }
        }
    }
}