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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ActionEvent
import java.io.Serial
import java.util.*
import javax.swing.*

const val WSL_PROXY_CONNECT_LOCALHOST = "wsl.proxy.connect.localhost"

abstract class AbstractRemoteConnectionDialog(
    protected val project: Project,
    parentComponent: Component,
    protected val settings: RemoteConnectionSettings,
    dialogTitle: String
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE) {

    private val originalScope = settings.scope
    protected lateinit var connectionNameTextField: JBTextField
    protected lateinit var urlPreviewLabel: JLabel
    protected lateinit var hostTextField: JBTextField
    protected lateinit var portTextField: JBTextField
    protected lateinit var sslProtocolCheckBox: JBCheckBox
    protected lateinit var webrootTextField: JBTextField
    protected lateinit var usernameTextField: JBTextField
    protected lateinit var passwordTextField: JBPasswordField
    protected lateinit var testConnectionLabel: Cell<JLabel>
    protected lateinit var testConnectionComment: Cell<JEditorPane>
    private lateinit var wslDistributionComboBox: JComboBox<String>
    private lateinit var wslProxyCheckBox: JBCheckBox
    private lateinit var wslProxyWarningComment: JEditorPane
    private lateinit var wslDistributionText: Cell<JLabel>
    protected var isWslCheckBox: JBCheckBox? = null
    private var testConnectionButton: Action = object : DialogWrapperAction("Test Connection") {

        @Serial
        private val serialVersionUID: Long = 7851071514284300449L

        override fun doAction(e: ActionEvent?) {
            this.isEnabled = false
            with(testConnectionLabel) {
                visible(true)

                component.text = "Executing test connection to remote host..."
                component.foreground = JBColor.LIGHT_GRAY
            }
            with(testConnectionComment) {
                visible(false)
            }

            ReadAction
                .nonBlocking<String?> {
                    testConnection(createTestSettings())
                }
                .finishOnUiThread(ModalityState.defaultModalityState()) {
                    with(testConnectionLabel) {
                        if (it.isNullOrBlank()) {
                            component.text = "Successfully connected to remote host with provided details."
                            component.foreground = ColorUtil.darker(JBColor.GREEN, 5)
                        } else {
                            component.text = "The host cannot be reached. Check the address and credentials."
                            component.foreground = ColorUtil.darker(JBColor.RED, 3)

                            with(testConnectionComment) {
                                text(it)
                                visible(true)
                            }
                        }
                    }

                    this.isEnabled = true
                }
                .submit(AppExecutorUtil.getAppExecutorService())
        }
    }

    protected abstract fun createTestSettings(): RemoteConnectionSettings
    protected abstract fun testConnection(testSettings: RemoteConnectionSettings): String?
    protected abstract fun panel(): DialogPanel

    init {
        title = dialogTitle
        super.init()
    }

    override fun applyFields() {
        super.applyFields()

        settings.credentials = null
        if (settings.uuid == null) {
            settings.uuid = UUID.randomUUID().toString()
        } else {
            // change of the scope
            if (originalScope != settings.scope) {
                RemoteConnectionUtil.changeRemoteConnectionScope(project, settings, originalScope)
            }
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Persisting credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes("SAP CX - ${settings.uuid}")
                PasswordSafe.instance.set(credentialAttributes, Credentials(usernameTextField.text, String(passwordTextField.password)))
            }
        })
    }

    override fun createCenterPanel() = with(panel()) {
        border = JBUI.Borders.empty(16)
        loadCredentials()
        this
    }

    private fun loadCredentials() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrieving credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                passwordTextField.text = settings.password
                passwordTextField.isEnabled = true

                usernameTextField.text = settings.username
                usernameTextField.isEnabled = true
            }
        })
    }

    override fun createLeftSideActions() = arrayOf(testConnectionButton)
    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = connectionNameTextField

    protected fun generateUrl() = RemoteConnectionUtil.generateUrl(
        sslProtocolCheckBox.isSelected,
        hostTextField.text,
        portTextField.text,
        webrootTextField.text,
    )

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
                .bindSelected(settings::isWsl)
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