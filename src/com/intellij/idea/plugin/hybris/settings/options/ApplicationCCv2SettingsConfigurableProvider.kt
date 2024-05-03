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
package com.intellij.idea.plugin.hybris.settings.options

import com.intellij.idea.plugin.hybris.common.equalsIgnoreOrder
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.ui.CCv2SubscriptionListPanel
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel

class ApplicationCCv2SettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "CCv2", "[y] SAP Commerce Cloud CCv2 configuration."
    ) {

        private val appSettings = ApplicationSettingsComponent.getInstance()
        private val state = appSettings.state
        private var originalCCv2Token: String? = ""
        private var originalCCv2Subscriptions = state.ccv2Subscriptions
            .map { it.clone() }

        private lateinit var ccv2TokenTextField: JBPasswordField
        private val ccv2SubscriptionListPanel = CCv2SubscriptionListPanel(originalCCv2Subscriptions)

        override fun createPanel() = panel {
            row {
                label("CCv2 token:")
                ccv2TokenTextField = passwordField()
                    .comment(
                        """
                            Specify developer specific Token for CCv2 API, it will be stored in the OS specific secure storage under <strong>SAP CX CCv2 Token</strong> alias.<br>
                            Official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html">help.sap.com - Generating API Tokens</a>.
                        """.trimIndent()
                    )
                    .align(AlignX.RIGHT)
                    .onIsModified { originalCCv2Token != String(ccv2TokenTextField.password) }
                    .onReset {
                        ccv2TokenTextField.isEnabled = false

                        appSettings.loadCCv2Token {
                            val ccv2Token = appSettings.ccv2Token
                            originalCCv2Token = ccv2Token

                            ccv2TokenTextField.text = ccv2Token
                            ccv2TokenTextField.isEnabled = true
                        }
                    }
                    .onApply {
                        appSettings.saveCCv2Token(String(ccv2TokenTextField.password)) {
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
                    .bindIntText(state::ccv2ReadTimeout)
            }

            group("CCv2 Subscriptions", false) {
                row {
                    cell(ccv2SubscriptionListPanel)
                        .align(AlignX.FILL)
                        .onApply { appSettings.setCCv2Subscriptions(ccv2SubscriptionListPanel.data) }
                        .onReset {
                            originalCCv2Subscriptions = state.ccv2Subscriptions
                                .map { it.clone() }
                            ccv2SubscriptionListPanel.data = originalCCv2Subscriptions
                        }
                        .onIsModified { ccv2SubscriptionListPanel.data.equalsIgnoreOrder(state.ccv2Subscriptions).not() }
                }
            }
        }
    }
}
