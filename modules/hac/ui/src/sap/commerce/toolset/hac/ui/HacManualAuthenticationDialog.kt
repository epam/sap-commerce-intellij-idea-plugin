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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CompletableDeferred
import sap.commerce.toolset.hac.auth.ProxyAuthCefRequestHandlerAdapter
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.util.concurrent.TimeUnit

/*
    It is mandatory to use MODELESS Modality type to ensure correct render of the Cef Browser
 */
class HacManualAuthenticationDialog(
    private val project: Project,
    private val settings: HacConnectionSettingsState,
    private val proxyCredentials: Credentials? = null,
    private val deferredCookies: CompletableDeferred<Map<String, String>>,
) : DialogWrapper(project, null, false, IdeModalityType.MODELESS) {

    private val jbCefBrowser = JBCefBrowser.createBuilder()
        .setOffScreenRendering(JBCefApp.isOffScreenRenderingModeEnabled())
        .setUrl(settings.generatedURL)
        .setCreateImmediately(true)
        .build()
        .apply {
            Disposer.register(disposable, this)

            setProperty(JBCefBrowser.Properties.FOCUS_ON_SHOW, true)

            if (settings.proxyAuthentication) {
                jbCefClient.addRequestHandler(
                    ProxyAuthCefRequestHandlerAdapter(project, proxyCredentials),
                    cefBrowser
                )
            }
        }

    init {
        title = "Authenticate via Browser"
        isResizable = false
        super.init()
    }

    override fun getInitialSize() = JBUI.DialogSizes.extraLarge()
    override fun createCenterPanel() = jbCefBrowser.component
    override fun getPreferredFocusedComponent() = jbCefBrowser.component

    override fun applyFields() {
        super.applyFields()
        val cookies = jbCefBrowser.getJBCefCookieManager().getCookies(null, false)
            .get(5, TimeUnit.SECONDS)
            .associate { it.name to it.value }
        deferredCookies.complete(cookies)
    }

    override fun doCancelAction() {
        super.doCancelAction()
        deferredCookies.complete(emptyMap())
    }
}