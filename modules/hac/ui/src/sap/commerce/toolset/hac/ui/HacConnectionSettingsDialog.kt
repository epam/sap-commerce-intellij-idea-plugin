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

import com.intellij.credentialStore.Credentials
import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import sap.commerce.toolset.exec.ExecConstants
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.generatedURL
import sap.commerce.toolset.exec.ui.ConnectionSettingsDialog
import sap.commerce.toolset.exec.ui.WSL_PROXY_CONNECT_LOCALHOST
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.awt.Component
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JEditorPane
import javax.swing.JLabel

class HacConnectionSettingsDialog(
    project: Project,
    parentComponent: Component,
    settings: HacConnectionSettingsState.Mutable
) : ConnectionSettingsDialog<HacConnectionSettingsState.Mutable>(project, parentComponent, settings, "Remote SAP Commerce Instance") {

    private lateinit var sslProtocolComboBox: ComboBox<String>
    private lateinit var sessionCookieNameTextField: JBTextField
    private lateinit var wslDistributionComboBox: JComboBox<String>
    private lateinit var wslProxyCheckBox: JBCheckBox
    private lateinit var wslProxyWarningComment: JEditorPane
    private lateinit var wslDistributionText: Cell<JLabel>
    private var isWslCheckBox: JBCheckBox? = null

    override fun testConnection(): String = HacHttpClient.getInstance(project).testConnection(
        HacConnectionSettingsState(
            host = hostTextField.text,
            port = portTextField.text,
            ssl = sslProtocolCheckBox.isSelected,
            wsl = isWslCheckBox?.isSelected ?: false,
            sslProtocol = sslProtocolComboBox.selectedItem?.toString() ?: "",
            webroot = webrootTextField.text,
            timeout = timeoutIntSpinner.number,
            sessionCookieName = sessionCookieNameTextField.text.takeIf { !it.isNullOrBlank() } ?: ExecConstants.DEFAULT_SESSION_COOKIE_NAME,
            credentials = Credentials(usernameTextField.text, String(passwordTextField.password)),
        )
    )

    override fun panel() = panel {
        row {
            label("Connection name:")
                .bold()
            connectionNameTextField = textField()
                .align(AlignX.FILL)
                .bindText(mutableSettings::name.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            label("Scope:")
                .comment("Non-personal settings will be stored in the <strong>hybrisProjectSettings.xml</strong> and can be shared via VCS.")
            comboBox(
                EnumComboBoxModel(ExecConnectionScope::class.java),
                renderer = SimpleListCellRenderer.create("?") { it.title }
            )
                .bindItem(mutableSettings::scope.toNullableProperty(ExecConnectionScope.PROJECT_PERSONAL))
        }.layout(RowLayout.PARENT_GRID)

        row {
            timeoutIntSpinner = spinner(1000 ..Int.MAX_VALUE, 1000)
                .label("Connection Timeout (ms):")
                .bindIntValue(mutableSettings::timeout)
                .component
        }.layout(RowLayout.PARENT_GRID)

        group("Full URL Preview", false) {
            row {
                urlPreviewLabel = label(mutableSettings.immutable().generatedURL)
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
                    .bindText(mutableSettings::host)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Address cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Port:")
                portTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(mutableSettings::port.toNonNullableProperty(""))
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
                    .bindSelected(mutableSettings::ssl)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
                sslProtocolComboBox = comboBox(
                    listOf(
                        "TLSv1",
                        "TLSv1.1",
                        "TLSv1.2"
                    ),
                    renderer = SimpleListCellRenderer.create("?") { it }
                )
                    .enabledIf(sslProtocolCheckBox.selected)
                    .bindItem(mutableSettings::sslProtocol.toNullableProperty())
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Webroot:")
                webrootTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(mutableSettings::webroot)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Session Cookie name:")
                sessionCookieNameTextField = textField()
                    .comment("Optional: override the session cookie name. Default is JSESSIONID.")
                    .align(AlignX.FILL)
                    .bindText(mutableSettings::sessionCookieName)
                    .apply { component.text = "" }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            if (isWindows()) {
                wslHostConfiguration()
            }
        }

        group("Credentials") {
            row {
                label("Username:")
                usernameTextField = textField()
                    .align(AlignX.FILL)
                    .enabled(false)
                    .addValidationRule("Username cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Password:")
                passwordTextField = passwordField()
                    .align(AlignX.FILL)
                    .enabled(false)
                    .addValidationRule("Password cannot be blank.") { it.password.isEmpty() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    fun updateWslIp(distributions: List<WSLDistribution>) {
        val wslIp = distributions.find { it.msId == wslDistributionComboBox.selectedItem as? String }
            ?.wslIpAddress
            ?.toString()
            ?.replace("/", "")
            ?: ""
        hostTextField.text = wslIp
    }

    fun isWindows() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")

    fun Panel.wslHostConfiguration() {
        val distributions = WslDistributionManager.getInstance().installedDistributions
        row {
            isWslCheckBox = checkBox("WSL")
                .bindSelected(mutableSettings::wsl)
                .selected(false)
                .visible(distributions.isNotEmpty())
                .onChanged {
                    val selected = isWslCheckBox?.isSelected ?: false
                    val multipleDistros = distributions.isNotEmpty()
                    wslDistributionComboBox.isVisible = selected && multipleDistros
                    wslDistributionText.visible(selected)
                    wslProxyCheckBox.isVisible = selected
                    wslProxyWarningComment.isVisible = selected
                    urlPreviewLabel.text = generateUrl()
                }
                .component
        }.layout(RowLayout.PARENT_GRID)
        val installedDistros = distributions.map { it.msId }
        if (installedDistros.isNotEmpty()) {
            row {
                wslDistributionText = label("WSL distribution:").visible(false)
                wslDistributionComboBox = comboBox(DefaultComboBoxModel(installedDistros.toTypedArray()))
                    .align(AlignX.FILL)
                    .visible(false)
                    .onChanged {
                        updateWslIp(distributions)
                    }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        } else {
            row {
                comment("No WSL distributions are installed.")
                    .visible(false)
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
        row {
            wslProxyCheckBox = checkBox("Enable wsl.proxy.connect.localhost")
                .comment("This will use the wsl.proxy.connect.localhost registry setting if available.")
                .visible(false)
                .selected(Registry.`is`(WSL_PROXY_CONNECT_LOCALHOST))
                .onChanged {
                    Registry.run { get(WSL_PROXY_CONNECT_LOCALHOST).setValue(!`is`(WSL_PROXY_CONNECT_LOCALHOST)) }
                    updateWslIp(distributions)
                }
                .component
        }.layout(RowLayout.PARENT_GRID)
        row {
            wslProxyWarningComment =
                comment("<strong>Warning:</strong> Connect to 127.0.0.1 on WSLProxy instead of public WSL IP which might be inaccessible due to routing issues.")
                    .visible(false)
                    .component
        }.layout(RowLayout.PARENT_GRID)
    }
}