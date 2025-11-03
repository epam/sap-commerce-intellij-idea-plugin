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

package sap.commerce.toolset.hac.ui

import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.and
import com.intellij.openapi.observable.util.equalsTo
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.layout.selected
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.ui.ConnectionSettingsDialog
import sap.commerce.toolset.hac.HacExecConstants
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.http.HacHttpAuthenticationResult
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.hac.exec.settings.state.AuthenticationMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.ui.inlineBanner
import sap.commerce.toolset.ui.repackDialog
import java.awt.Component
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel

class HacConnectionSettingsDialog(
    project: Project,
    parentComponent: Component,
    settings: HacConnectionSettingsState.Mutable,
    dialogTitle: String,
) : ConnectionSettingsDialog<HacConnectionSettingsState.Mutable>(project, parentComponent, settings, dialogTitle) {

    private lateinit var urlPreviewLabel: JLabel
    private lateinit var timeoutIntSpinner: JBIntSpinner
    private lateinit var usernameTextField: JBTextField
    private lateinit var passwordTextField: JBPasswordField
    private lateinit var sslProtocolComboBox: ComboBox<String>
    private lateinit var sessionCookieNameTextField: JBTextField
    private lateinit var wslDistributionComboBox: JComboBox<WSLDistribution>

    override fun retrieveCredentials(mutable: HacConnectionSettingsState.Mutable) = HacExecConnectionService.getInstance(project)
        .getCredentials(mutable.immutable().first)

    override fun testConnection(): String? = HacHttpClient.getInstance(project).testConnection(
        HacConnectionSettingsState(
            host = hostTextField.text,
            port = portTextField.text,
            ssl = sslProtocolCheckBox.isSelected,
            wsl = mutable.wsl.get(),
            sslProtocol = sslProtocolComboBox.selectedItem?.toString() ?: "",
            webroot = webrootTextField.text,
            timeout = timeoutIntSpinner.number,
            sessionCookieName = sessionCookieNameTextField.text.takeIf { !it.isNullOrBlank() } ?: ExecConstants.DEFAULT_SESSION_COOKIE_NAME,
        ),
        mutable.username.get(),
        mutable.password.get(),
    )
        .let {
            when {
                it is HacHttpAuthenticationResult.Error -> it.message
                else -> null
            }
        }

    override fun panel() = panel {
        row {
            label("Connection name:")
                .bold()
            connectionNameTextField = textField()
                .align(AlignX.FILL)
                .bindText(mutable::name.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            label("Scope:")
                .comment("Non-personal settings will be stored in the <strong>hybrisProjectSettings.xml</strong> and can be shared via VCS.")
            comboBox(
                EnumComboBoxModel(ExecConnectionScope::class.java),
                renderer = SimpleListCellRenderer.create("?") { it.title }
            )
                .bindItem(mutable::scope.toNullableProperty(ExecConnectionScope.PROJECT_PERSONAL))
        }.layout(RowLayout.PARENT_GRID)

        row {
            timeoutIntSpinner = spinner(1000..Int.MAX_VALUE, 1000)
                .label("Connection timeout:")
                .bindIntValue(mutable::timeout)
                .gap(RightGap.SMALL)
                .component
            label("(ms)")
        }.layout(RowLayout.PARENT_GRID)

        group("Full URL Preview", false) {
            row {
                urlPreviewLabel = label(mutable.generatedURL)
                    .bold()
                    .align(AlignX.FILL)
                    .component
            }
            row {
                testConnectionLabel = label("")
                    .visible(false)
            }
            row {
                testConnectionComment = comment("")
                    .visible(false)
            }
        }

        group("Host Settings") {
            row {
                label("Address:")
                hostTextField = textField()
                    .comment("Host name or IP address")
                    .align(AlignX.FILL)
                    .bindText(mutable::host)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Address cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Port:")
                portTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(mutable::port.toNonNullableProperty(""))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Port should be blank or in a range of 1..65535.") {
                        if (it.text.isNullOrBlank()) return@addValidationRule false

                        val intValue = it.text.toIntOrNull() ?: return@addValidationRule true
                        return@addValidationRule intValue !in 1..65535
                    }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                sslProtocolCheckBox = checkBox("SSL:")
                    .bindSelected(mutable::ssl)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
                sslProtocolComboBox = comboBox(
                    listOf(
                        "TLSv1",
                        "TLSv1.1",
                        "TLSv1.2",
                        "TLSv1.3",
                    ),
                    renderer = SimpleListCellRenderer.create("?") { it }
                )
                    .enabledIf(sslProtocolCheckBox.selected)
                    .bindItem(mutable::sslProtocol.toNullableProperty())
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Webroot:")
                webrootTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(mutable::webroot)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Session Cookie name:")
                sessionCookieNameTextField = textField()
                    .comment("Optional: override the session cookie name. Default is JSESSIONID.")
                    .align(AlignX.FILL)
                    .bindText(mutable::sessionCookieName)
                    .apply { component.text = "" }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }

        if (SystemInfo.isWindows) {
            group("Windows Subsystem for Linux") {
                wslHostConfiguration()
            }
        }

        group("Authentication") {
            row {
                segmentedButton(AuthenticationMode.entries.toList()) {
                    text = it.title
                    toolTipText = it.description
                }
                    .align(AlignX.CENTER)
                    .bind(mutable.authenticationMode)
                    .whenItemSelected(disposable) {
                        repackDialog()
                    }
            }

            authenticationAutomatic()
            authenticationManual()
        }
    }

    private fun Panel.authenticationManual() {
        if (!JBCefApp.isSupported()) {
            row {
                inlineBanner("Set the reg key to enable JCEF:\n\"ide.browser.jcef.enabled=true\"", EditorNotificationPanel.Status.Warning)
            }
                .visibleIf(mutable.authenticationMode.equalsTo(AuthenticationMode.MANUAL))
                .topGap(TopGap.MEDIUM)
                .bottomGap(BottomGap.MEDIUM)
        }



        row {
            label("Authentication request via Browser will happen on Api request to hAC.")
                .align(AlignX.CENTER)
                .visibleIf(mutable.authenticationMode.equalsTo(AuthenticationMode.MANUAL))
        }
    }

    private fun Panel.authenticationAutomatic() {
        row {
            usernameTextField = textField()
                .label("Username:")
                .align(AlignX.FILL)
                .bindText(mutable.username)
                .enabledIf(editableCredentials)
                .visibleIf(mutable.authenticationMode.equalsTo(AuthenticationMode.AUTOMATIC))
                .addValidationRule("Username cannot be blank.") { it.text.isNullOrBlank() }
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            passwordTextField = passwordField()
                .label("Password:")
                .align(AlignX.FILL)
                .bindText(mutable.password)
                .enabledIf(editableCredentials)
                .visibleIf(mutable.authenticationMode.equalsTo(AuthenticationMode.AUTOMATIC))
                .addValidationRule("Password cannot be blank.") { it.password.isEmpty() }
                .component
        }.layout(RowLayout.PARENT_GRID)
    }

    private fun updateWslIp(distributions: List<WSLDistribution>) {
        val wslIp = distributions
            .find { it == wslDistributionComboBox.selectedItem }
            ?.wslIpAddress
            ?.toString()
            ?.replace("/", "")
            ?: ""
        hostTextField.text = wslIp
    }

    private fun Panel.wslHostConfiguration() {
        val wslDistributions: AtomicProperty<List<WSLDistribution>> = AtomicProperty(WslDistributionManager.getInstance().installedDistributions)

        row {
            inlineBanner(
                message = """
                <p>Find out why using <a href="https://www.linkedin.com/pulse/high-performance-sap-commerce-development-windows-using-de-matola-gwgvf/">WSL</a> can boost the development process!</p>
                """.trimIndent(),
                icon = HybrisIcons.Tools.WSL
            )
                .align(AlignX.FILL)
                .gap(RightGap.COLUMNS)
        }
            .topGap(TopGap.MEDIUM)
            .bottomGap(BottomGap.MEDIUM)

        row {
            checkBox("Connect to WSL")
                .bindSelected(mutable.wsl)
                .onChanged {
                    urlPreviewLabel.text = generateUrl()
                    repackDialog()
                }
        }.layout(RowLayout.PARENT_GRID)

        row {
            inlineBanner("No WSL distributions are installed.", EditorNotificationPanel.Status.Warning)
                .visibleIf(mutable.wsl.and(wslDistributions.transform { it.isEmpty() }))
                .align(AlignX.FILL)
                .gap(RightGap.COLUMNS)
        }
            .topGap(TopGap.MEDIUM)
            .bottomGap(BottomGap.MEDIUM)

        row {
            val model = DefaultComboBoxModel(wslDistributions.get().toTypedArray())
            wslDistributionComboBox = comboBox(
                model = model,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    label.text = value?.msId
                })
                .label("WSL distribution:")
                .visibleIf(mutable.wsl)
                .enabledIf(wslDistributions.transform { it.isNotEmpty() })
                .align(AlignX.FILL)
                .onChanged {
                    updateWslIp(wslDistributions.get())
                }
                .resizableColumn()
                .gap(RightGap.SMALL)
                .component

            button("Refresh") {
                wslDistributions.set(WslDistributionManager.getInstance().installedDistributions)
                with(model) {
                    removeAllElements()
                    addAll(wslDistributions.get())
                }
                repackDialog()
            }
                .align(AlignX.RIGHT)
                .visibleIf(mutable.wsl)
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Enable wsl.proxy.connect.localhost")
                .comment("This will use the wsl.proxy.connect.localhost registry setting if available.")
                .visibleIf(mutable.wsl)
                .enabledIf(wslDistributions.transform { it.isNotEmpty() })
                .selected(Registry.`is`(HacExecConstants.WSL_PROXY_CONNECT_LOCALHOST, false))
                .onChanged {
                    Registry.run {
                        val registryValue = get(HacExecConstants.WSL_PROXY_CONNECT_LOCALHOST)
                        registryValue.setValue(!`is`(HacExecConstants.WSL_PROXY_CONNECT_LOCALHOST, false))
                    }
                    updateWslIp(wslDistributions.get())
                }
        }.layout(RowLayout.PARENT_GRID)

        row {
            comment("<strong>Warning:</strong> Connect to 127.0.0.1 on WSLProxy instead of public WSL IP which might be inaccessible due to routing issues.")
                .visibleIf(mutable.wsl)
        }.layout(RowLayout.PARENT_GRID)
    }
}