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

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.api.CCv2AuthToken
import sap.commerce.toolset.ccv2.api.LegacyAuthToken
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Authentication
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionListPanel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModel
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.ui.inlineBanner

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

        private lateinit var endpointTextField: JBTextField
        private lateinit var resourceTextField: JBTextField
        private lateinit var clientIdTextField: JBPasswordField
        private lateinit var clientSecretTextField: JBPasswordField

        private val editable = AtomicBooleanProperty(false)
        private val projectSettings = CCv2ProjectSettings.getInstance()
        private val developerSettings = CCv2DeveloperSettings.getInstance(project)
        private val mutable = projectSettings.state.mutable()
        private var originalToken: String? = null
        private var originalClientId: String? = null
        private var originalClientSecret: String? = null
        private var originalActiveSubscription = developerSettings.getActiveCCv2Subscription()
        private lateinit var pane: DialogPanel

        override fun createPanel(): DialogPanel {
            // disposable is being created only now, do not move dependant items
            subscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true, disposable = disposable)
            val ccv2LegacyTokenSupplier: () -> CCv2AuthToken? = {
                defaultCCv2TokenTextField.password?.let { LegacyAuthToken(String(it)) }
            }
            val ccv2ClientTokenSupplier: () -> CCv2AuthToken? = {
                val auth = CCv2Authentication(endpointTextField.text, resourceTextField.text)
                val credentials = Credentials(String(clientIdTextField.password), String(clientSecretTextField.password))

                CCv2Service.getInstance(project).retrieveAuthToken(auth, credentials)
            }
            subscriptionListPanel = CCv2SubscriptionListPanel(project, disposable, ccv2LegacyTokenSupplier, ccv2ClientTokenSupplier) {
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
                    label("Read timeout:")
                    timeoutTextField = intTextField(10..Int.MAX_VALUE)
                        .comment("Indicates read timeout in seconds when invoking Cloud Portal API.")
                        .bindIntText(projectSettings::readTimeout)
                        .enabledIf(editable)
                        .commentRight("(seconds)")
                        .component
                }.layout(RowLayout.PARENT_GRID)

                authToken()
                authClient()

                group("Subscriptions", false) {
                    row {
                        cell(subscriptionListPanel)
                            .onIsModified { subscriptionListPanel.data.any { it.modified } }
                            .align(AlignX.FILL)
                    }
                }
            }
                .also { pane = it }
        }

        override fun reset() {
            super.reset()
            subscriptionListPanel.data = projectSettings.state.mutable().subscriptions
            subscriptionsComboBoxModel.refresh(subscriptionListPanel.data.map { it.immutable() })

            activeCCv2SubscriptionComboBox.selectedItem = originalActiveSubscription
            defaultCCv2TokenTextField.text = originalToken
            clientIdTextField.text = originalClientId
            clientSecretTextField.text = originalClientSecret

            initForm()
        }

        override fun apply() {
            super.apply()

            developerSettings.activeCCv2SubscriptionID = activeCCv2SubscriptionComboBox.selectedItem?.asSafely<CCv2Subscription>()?.uuid
            projectSettings.subscriptions = subscriptionListPanel.data
                .onEach {
                    if (it.modified) {
                        projectSettings.saveCCv2Token(it.uuid, it.ccv2Token)
                        projectSettings.saveCCv2Authentication(it.uuid, it.authentication.credentials)
                    }
                    it.modified = false
                }
                .map { it.immutable() }

            val clientId = String(clientIdTextField.password)
            val clientSecret = String(clientSecretTextField.password)
            val credentials = Credentials(clientId, clientSecret)
                .takeUnless { it.userName.isNullOrBlank() || it.password.isNullOrBlank() }
            projectSettings.saveDefaultCCv2Authentication(credentials)

            originalActiveSubscription = developerSettings.getActiveCCv2Subscription()
        }

        private fun initForm() {
            var expectedLoads = 2

            projectSettings.loadDefaultCCv2Token {
                val ccv2Token = projectSettings.getCCv2Token()

                defaultCCv2TokenTextField.text = ccv2Token
                originalToken = ccv2Token

                expectedLoads--
                if (expectedLoads == 0) {
                    editable.set(true)
                }
            }

            projectSettings.loadDefaultCCv2Authentication { credentials ->
                credentials?.let {
                    originalClientId = it.userName ?: ""
                    originalClientSecret = it.getPasswordAsString() ?: ""
                    clientIdTextField.text = originalClientId
                    clientSecretTextField.text = originalClientSecret
                }

                expectedLoads--
                if (expectedLoads == 0) {
                    editable.set(true)
                }
            }
        }

        private fun Panel.authToken() = group("Authentication via Token") {
            row {
                defaultCCv2TokenTextField = passwordField()
                    .label(i18n("hybris.settings.application.ccv2Token"))
                    .comment(i18n("hybris.settings.application.ccv2Token.tooltip"))
                    .align(AlignX.FILL)
                    .onIsModified { (originalToken ?: "") != String(defaultCCv2TokenTextField.password) }
                    .onApply {
                        val token = String(defaultCCv2TokenTextField.password).takeIf { it.isNotBlank() }
                        originalToken = token
                        projectSettings.saveDefaultCCv2Token(token)
                    }
                    .component
                contextHelp(i18n("hybris.settings.application.ccv2Token.help.description"))
            }.layout(RowLayout.PARENT_GRID)
        }

        private fun Panel.authClient() = group("Authentication via Technical User") {
            row {
                inlineBanner(
                    message = """
                        Experimental authentication mode, see more here <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/edcfd89aa5154be59910ebb7081030e3.html">Migration of Cloud Portal to Kyma Runtime</a>.
                        """.trimIndent(),
                    status = EditorNotificationPanel.Status.Warning
                )
            }

            row {
                endpointTextField = textField()
                    .label("Token endpoint:")
                    .align(AlignX.FILL)
                    .bindText(mutable.authentication::tokenEndpoint)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                resourceTextField = textField()
                    .label("Resource:")
                    .align(AlignX.FILL)
                    .bindText(mutable.authentication::resource)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                clientIdTextField = passwordField()
                    .label("Client id:")
                    .align(AlignX.FILL)
                    .onIsModified { (originalClientId ?: "") != String(clientIdTextField.password) }
                    .onApply { originalClientId = String(clientIdTextField.password) }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                clientSecretTextField = passwordField()
                    .label("Client secret:")
                    .align(AlignX.FILL)
                    .onIsModified { (originalClientSecret ?: "") != String(clientSecretTextField.password) }
                    .onApply { originalClientSecret = String(clientSecretTextField.password) }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
            .enabledIf(editable)
    }
}