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

package sap.commerce.toolset.ccv2.ui

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.equalsTo
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv1.model.SubscriptionDTO
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.api.ApiContext
import sap.commerce.toolset.ccv2.api.HanaApiContext
import sap.commerce.toolset.ccv2.api.KymaApiContext
import sap.commerce.toolset.ccv2.event.CCv2SubscriptionsListener
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Authentication
import sap.commerce.toolset.ccv2.settings.state.CCv2AuthenticationMode
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ui.contextHelp
import sap.commerce.toolset.ui.repackDialog
import sap.commerce.toolset.ui.scrollPanel
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.io.Serial

internal class CCv2SubscriptionDialog(
    private val project: Project,
    parentComponent: Component,
    private val subscription: CCv2Subscription.Mutable,
    dialogTitle: String,
    private val ccv2LegacyTokenSupplier: () -> ApiContext?,
    private val ccv2ClientTokenSupplier: () -> ApiContext?,
    private val hanaApiUrlSupplier: () -> String,
    private val kymaApiUrlSupplier: () -> String,
) : DialogWrapper(null, parentComponent, false, IdeModalityType.IDE) {

    private val beforeModification = subscription.copy(
        authentication = subscription.authentication.copy(),
    )
    private val enabled by lazy { AtomicBooleanProperty(subscription.id == null) }
    private val showSubscriptions = AtomicBooleanProperty(false)

    private lateinit var idTextField: JBTextField
    private lateinit var nameTextField: JBTextField
    private lateinit var subscriptionCCv2TokenField: JBPasswordField
    private lateinit var subscriptionCCv2EndpointField: JBTextField
    private lateinit var subscriptionCCv2ResourceField: JBTextField
    private lateinit var subscriptionCCv2ClientIdField: JBPasswordField
    private lateinit var subscriptionCCv2ClientSecretField: JBPasswordField
    private val subscriptionsPanel = JBPanel<JBPanel<*>>(GridBagLayout())
        .also {
            it.border = JBUI.Borders.empty()
            it.maximumSize = Dimension(Int.MAX_VALUE, 200)
        }
    private val authModeObservable = AtomicProperty(subscription.authenticationMode).apply {
        afterChange(disposable) {
            val panel = CCv2ToolWindowUtil.noDataPanel("Re-fetch subscriptions...")

            subscriptionsPanel.removeAll()
            subscriptionsPanel.add(panel)

            repackDialog()
        }
    }

    private val fetchSubscriptionsButton = object : DialogWrapperAction("Fetch Subscriptions") {
        @Serial
        private val serialVersionUID: Long = -6131274561037160651L

        override fun doAction(e: ActionEvent) {
            if (showSubscriptions.get()) {
                val authToken = getSubscriptionToken()
                    ?: HanaApiContext(hanaApiUrlSupplier(), "")
                initSubscriptionsPanel(authToken)
            }
        }
    }

    init {
        title = dialogTitle
        isResizable = false

        super.init()

        subscribe()

        if (!subscription.ccv2LegacyTokenLoaded) {
            CCv2ProjectSettings.getInstance().loadCCv2Token(subscription.uuid) { token ->
                subscriptionCCv2TokenField.text = token
                subscription.ccv2LegacyTokenLoaded = true
                subscription.ccv2Token = token

                beforeModification.ccv2Token = subscription.ccv2Token

                if (subscription.authenticationMode == CCv2AuthenticationMode.TOKEN) {
                    enabled.set(true)

                    val authToken = token
                        ?.let { HanaApiContext(hanaApiUrlSupplier(), token) }
                        ?: ccv2LegacyTokenSupplier()
                        ?: HanaApiContext(hanaApiUrlSupplier(), "")
                    initSubscriptionsPanel(authToken)
                }
            }
        } else if (subscription.authenticationMode == CCv2AuthenticationMode.TOKEN) initWithNotPersistedToken()

        if (!subscription.ccv2ClientTokenLoaded) {
            CCv2ProjectSettings.getInstance().loadCCv2Authentication(subscription.uuid) { credentials ->
                subscription.ccv2ClientTokenLoaded = true
                subscription.authentication.clientId = credentials?.userName ?: ""
                subscription.authentication.clientSecret = credentials?.getPasswordAsString() ?: ""

                subscriptionCCv2ClientIdField.text = subscription.authentication.clientId
                subscriptionCCv2ClientSecretField.text = subscription.authentication.clientSecret

                beforeModification.authentication.clientId = subscription.authentication.clientId
                beforeModification.authentication.clientSecret = subscription.authentication.clientSecret

                if (subscription.authenticationMode == CCv2AuthenticationMode.TECHNICAL_USER) {
                    enabled.set(true)

                    val authToken = credentials
                        ?.let { CCv2Service.getInstance(project).retrieveAuthToken(kymaApiUrlSupplier(), subscription.authentication.immutable(), it) }
                        ?: ccv2ClientTokenSupplier()
                        ?: KymaApiContext(kymaApiUrlSupplier(), "")
                    initSubscriptionsPanel(authToken)
                }
            }
        } else if (subscription.authenticationMode == CCv2AuthenticationMode.TECHNICAL_USER) initWithNotPersistedToken()
    }

    override fun createLeftSideActions() = arrayOf(fetchSubscriptionsButton)
    override fun getPreferredFocusedComponent() = nameTextField

    override fun applyFields() {
        super.applyFields()

        if (beforeModification != subscription) {
            subscription.modified = true
        }
    }

    override fun createCenterPanel() = panel {
        row {
            nameTextField = textField()
                .label("Name:")
                .align(AlignX.FILL)
                .bindText(subscription::name.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            idTextField = textField()
                .label("Subscription code:")
                .align(AlignX.FILL)
                .addValidationRule("ID cannot be blank.") { it.text.isBlank() }
                .bindText(subscription::id.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            comboBox(
                model = EnumComboBoxModel(CCv2AuthenticationMode::class.java),
                renderer = SimpleListCellRenderer.create("?") { it.presentationTitle }
            )
                .label("Auth mode:")
                .bindItem(subscription::authenticationMode.toNullableProperty())
                .onChanged { authModeObservable.set(it.selectedItem as CCv2AuthenticationMode) }
        }.layout(RowLayout.PARENT_GRID)

        authToken()
        authClient()

        group("Available Subscriptions") {
            row {
                comment("Select a subscription to fill-in the <cite>Subscription code</cite> and <cite>Name</cite>.")
            }.bottomGap(BottomGap.SMALL)

            row {
                cell(subscriptionsPanel)
            }.visibleIf(showSubscriptions)

            row {
                panel {
                    row {
                        icon(AnimatedIcon.Default.INSTANCE)
                        label("Retrieving subscriptions...")
                    }
                }.align(Align.CENTER)
            }.visibleIf(showSubscriptions.not())
        }
    }

    private fun Panel.authToken() = indent {
        row {
            subscriptionCCv2TokenField = passwordField()
                .label("CCv2 token:")
                .enabledIf(enabled)
                .comment("Overrides default CCv2 token per subscription.")
                .align(AlignX.FILL)
                .bindText(subscription::ccv2Token.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)
    }
        .visibleIf(authModeObservable.equalsTo(CCv2AuthenticationMode.TOKEN))

    private fun Panel.authClient() = indent {
        row {
            subscriptionCCv2EndpointField = textField()
                .label("Token endpoint:")
                .align(AlignX.FILL)
                .bindText(subscription.authentication::tokenEndpoint)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            subscriptionCCv2ResourceField = textField()
                .label("Resource:")
                .align(AlignX.FILL)
                .bindText(subscription.authentication::resource)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            subscriptionCCv2ClientIdField = passwordField()
                .label("Client id:")
                .align(AlignX.FILL)
                .bindText(subscription.authentication::clientId)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            subscriptionCCv2ClientSecretField = passwordField()
                .label("Client secret:")
                .align(AlignX.FILL)
                .bindText(subscription.authentication::clientSecret)
                .component
        }.layout(RowLayout.PARENT_GRID)
    }
        .enabledIf(enabled)
        .visibleIf(authModeObservable.equalsTo(CCv2AuthenticationMode.TECHNICAL_USER))

    private fun initWithNotPersistedToken() {
        enabled.set(true)

        val authToken = getSubscriptionToken()
            ?: HanaApiContext(hanaApiUrlSupplier(), "")
        initSubscriptionsPanel(authToken)
    }

    private fun subscribe() {
        project.messageBus.connect(disposable).subscribe(CCv2SubscriptionsListener.TOPIC, object : CCv2SubscriptionsListener {
            override fun onFetchingComplete(data: Collection<SubscriptionDTO>) {
                val panel = if (data.isNotEmpty()) subscriptionsPanel(data)
                else CCv2ToolWindowUtil.noDataPanel("No subscriptions found for the given token.")

                subscriptionsPanel.add(panel)
                showSubscriptions.set(true)
                peer.window?.pack()
            }

            override fun onFetchingError(e: Throwable) {
                val panel = CCv2ToolWindowUtil.noDataPanel("Unable to get subscriptions due: </br>${e.message}", EditorNotificationPanel.Status.Error)
                subscriptionsPanel.add(panel)
                showSubscriptions.set(true)
                peer.window?.pack()
            }
        })
    }

    private fun subscriptionsPanel(data: Collection<SubscriptionDTO>) = panel {
        data
            .sortedBy { it.customerName }
            .forEach { subscriptionDto ->
                row {
                    panel {
                        row {
                            contextHelp(
                                HybrisIcons.Module.CCV2, """
                                <pre>
 · code:                     ${subscriptionDto.code}
 · name:                     ${subscriptionDto.name ?: "N/A"}
 · external code:            ${subscriptionDto.externalCode ?: "N/A"}
 · status:                   ${subscriptionDto.status ?: "N/A"}
 · customer code:            ${subscriptionDto.customerCode ?: "N/A"}
 · customer SAP Internal Id: ${subscriptionDto.customerSapInternalId ?: "N/A"}
 · region code:              ${subscriptionDto.regionCode ?: "N/A"}
 · spc id:                   ${subscriptionDto.spcId ?: "N/A"}
 · internal:                 ${subscriptionDto.internal ?: "N/A"}
 · spn:                      ${subscriptionDto.spn ?: "N/A"}
 · disaster recovery type:   ${subscriptionDto.disasterRecoveryType ?: "N/A"}
 · digital wallet:           ${subscriptionDto.digitalWalletActivated ?: "N/A"}</pre>
                            """.trimIndent(),
                                "Subscription Details"
                            )
                            label(subscriptionDto.customerName?.let { StringUtil.first(it, 40, true) } ?: "N/A")
                                .comment("Name")
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            label(subscriptionDto.regionName ?: "N/A")
                                .comment("Region")
                        }
                    }.gap(RightGap.COLUMNS)

                    panel {
                        row {
                            button("Select") {
                                idTextField.text = subscriptionDto.code
                                nameTextField.text = subscriptionDto.customerName
                            }
                        }
                    }
                }
                    .layout(RowLayout.PARENT_GRID)
            }
    }
        .apply { border = JBUI.Borders.emptyRight(16) }
        .let { scrollPanel(it) }
        .apply { preferredSize = Dimension(preferredSize.width, 180) }

    private fun getSubscriptionToken(): ApiContext? = if (authModeObservable.get() == CCv2AuthenticationMode.TOKEN) {
        subscriptionCCv2TokenField.password
            ?.let { String(it) }
            ?.takeIf { it.isNotBlank() }
            ?.let { HanaApiContext(hanaApiUrlSupplier(), it) }
            ?: ccv2LegacyTokenSupplier()
    } else {
        val tokenEndpoint = subscriptionCCv2EndpointField.text.takeIf { it.isNotBlank() } ?: return ccv2ClientTokenSupplier()
        val resource = subscriptionCCv2ResourceField.text.takeIf { it.isNotBlank() } ?: return ccv2ClientTokenSupplier()
        val auth = CCv2Authentication(tokenEndpoint, resource)
        val credentials = subscription.authentication.credentials
            ?: return ccv2ClientTokenSupplier()

        CCv2Service.getInstance(project).retrieveAuthToken(kymaApiUrlSupplier(), auth, credentials)
            ?: ccv2ClientTokenSupplier()
    }

    private fun initSubscriptionsPanel(token: ApiContext) {
        showSubscriptions.set(false)
        subscriptionsPanel.removeAll()

        CCv2Service.getInstance(project).fetchSubscriptions(token)
    }
}
