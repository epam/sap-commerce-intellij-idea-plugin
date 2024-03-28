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
import com.intellij.idea.plugin.hybris.tools.ccv2.strategies.CCv2Strategy
import com.intellij.idea.plugin.hybris.ui.CCv2SubscriptionListPanel
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*

class ApplicationCCv2SettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "CCv2", "[y] SAP Commerce Cloud CCv2 configuration."
    ) {

        private val appSettings = ApplicationSettingsComponent.getInstance()
        private val state = appSettings.state
        private var originalSAPCLIToken: String? = ""
        private var originalCCv2Subscriptions = state.ccv2Subscriptions
            .map { it.clone() }

        private lateinit var sapCLITokenTextField: JBPasswordField
        private val ccv2SubscriptionListPanel = CCv2SubscriptionListPanel(originalCCv2Subscriptions)

        // TODO: we may use SAP CCM CLI or native REST API in the future
        override fun createPanel() = panel {
            row {}.comment(
                """
                    All details on using SAP CCM can be found in official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9116f1cfd16049c3a531bfb6a681ff77/8acde53272c64efb908b9f0745498015.html">help.sap.com - Command Line Interface</a>.
                """.trimIndent()
            )

            row {
                label("CLI directory:")
                cell(
                    textFieldWithBrowseButton(
                        null,
                        "Select SAP CX CLI Directory",
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .comment(
                        """
                            SAP Commerce Cloud command line interface installation directory.<br>
                            Choose directory extracted from the <strong>CXCOMMCLI00P_*.zip</strong> file to enable CCv2 CLI integration. 
                        """.trimIndent()
                    )
                    .bindText(
                        { state.sapCLIDirectory ?: "" },
                        { state.sapCLIDirectory = it }
                    )
                    .validationOnInput {
                        CCv2Strategy.getSAPCCMCCv2Strategy().validateSAPCCMDirectory(it.text)
                            ?.let { message -> this.error(message) }
                    }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("CLI token:")
                sapCLITokenTextField = passwordField()
                    .comment(
                        """
                            Specify developer specific Token for CCv2 CLI, it will be stored in the OS specific secure storage under <strong>SAP CX CLI Token</strong> alias.<br>
                            Official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html">help.sap.com - Generating API Tokens</a>.
                        """.trimIndent()
                    )
                    .align(AlignX.RIGHT)
                    .onIsModified { originalSAPCLIToken != String(sapCLITokenTextField.password) }
                    .onReset {
                        sapCLITokenTextField.isEnabled = false

                        appSettings.loadSAPCLIToken {
                            val sapCLIToken = appSettings.ccv2Token
                            originalSAPCLIToken = sapCLIToken

                            sapCLITokenTextField.text = sapCLIToken
                            sapCLITokenTextField.isEnabled = true
                        }
                    }
                    .onApply {
                        appSettings.saveSAPCLIToken(String(sapCLITokenTextField.password)) {
                            originalSAPCLIToken = it
                        }
                    }
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Connection timeout:")
                intTextField(10..Int.MAX_VALUE)
                    .comment(
                        """
                            Indicates waiting time in seconds when invoking SAP CCM.
                        """.trimIndent()
                    )
                    .bindIntText(state::sapCLITimeout)
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
