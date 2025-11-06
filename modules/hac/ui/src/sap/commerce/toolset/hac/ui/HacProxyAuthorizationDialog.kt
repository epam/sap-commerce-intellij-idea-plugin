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
import com.intellij.ide.IdeBundle
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class HacProxyAuthorizationDialog(
    project: Project,
    private val proxy: String,
    proxyCredentials: Credentials? = null
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    private val username = AtomicProperty(proxyCredentials?.userName ?: "")
    private val password = AtomicProperty(proxyCredentials?.getPasswordAsString() ?: "")

    var credentials: Credentials? = null
        private set

    private lateinit var usernameTextField: JBTextField

    init {
        super.init()

        title = IdeBundle.message("dialog.title.jcef.proxyAuthentication")
        isResizable = false

        setOKButtonText(IdeBundle.message("dialog.button.ok.jcef.signIn"))
    }

    override fun getPreferredFocusedComponent() = usernameTextField

    override fun createCenterPanel() = panel {
        row {
            label(IdeBundle.message("dialog.content.jcef.proxyServer", proxy))
        }

        separator()

        row {
            usernameTextField = textField()
                .label(IdeBundle.message("dialog.content.label.jcef.login"))
                .bindText(username)
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)

        row {
            passwordField()
                .label(IdeBundle.message("dialog.content.label.jcef.password"))
                .bindText(password)
                .align(AlignX.FILL)
        }
            .layout(RowLayout.PARENT_GRID)
    }

    override fun applyFields() {
        super.applyFields()
        credentials = Credentials(username.get(), password.get())
    }
}