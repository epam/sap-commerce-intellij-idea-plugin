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

package sap.commerce.toolset.exec.ui

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.text
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import java.awt.Component
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.Action
import javax.swing.JEditorPane
import javax.swing.JLabel

const val WSL_PROXY_CONNECT_LOCALHOST = "wsl.proxy.connect.localhost"

abstract class ConnectionSettingsDialog<T: ExecConnectionSettingsState.Mutable>(
    protected val project: Project,
    parentComponent: Component,
    protected val mutableSettings: T,
    dialogTitle: String
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE) {

    protected lateinit var connectionNameTextField: JBTextField
    protected lateinit var urlPreviewLabel: JLabel
    protected lateinit var hostTextField: JBTextField
    protected lateinit var portTextField: JBTextField
    protected lateinit var sslProtocolCheckBox: JBCheckBox
    protected lateinit var timeoutIntSpinner: JBIntSpinner
    protected lateinit var webrootTextField: JBTextField
    protected lateinit var usernameTextField: JBTextField
    protected lateinit var passwordTextField: JBPasswordField
    protected lateinit var testConnectionLabel: Cell<JLabel>
    protected lateinit var testConnectionComment: Cell<JEditorPane>
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
                    testConnection()
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

    protected abstract fun testConnection(): String?
    protected abstract fun panel(): DialogPanel

    init {
        title = dialogTitle
        super.init()
    }

    override fun applyFields() {
        super.applyFields()

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Persisting credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes("SAP CX - ${mutableSettings.uuid}")
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
                passwordTextField.text = mutableSettings.password
                passwordTextField.isEnabled = true

                usernameTextField.text = mutableSettings.username
                usernameTextField.isEnabled = true
            }
        })
    }

    override fun createLeftSideActions() = arrayOf(testConnectionButton)
    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = connectionNameTextField

    protected fun generateUrl() = sap.commerce.toolset.exec.generateUrl(
        sslProtocolCheckBox.isSelected,
        hostTextField.text,
        portTextField.text,
        webrootTextField.text,
    )
}