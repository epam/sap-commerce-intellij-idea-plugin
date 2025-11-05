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

package sap.commerce.toolset.hac.auth

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import org.cef.browser.CefBrowser
import org.cef.callback.CefAuthCallback
import org.cef.handler.CefRequestHandlerAdapter
import sap.commerce.toolset.hac.ui.HacProxyAuthenticatorDialog
import java.awt.EventQueue
import java.awt.GraphicsEnvironment

internal class ProxyAuthCefRequestHandlerAdapter(
    private val project: Project,
    private val proxyCredentials: Credentials?,
    private val authorizationRef: Ref<Credentials?>,
) : CefRequestHandlerAdapter() {

    override fun getAuthCredentials(
        browser: CefBrowser,
        originUrl: String,
        isProxy: Boolean,
        host: String,
        port: Int,
        realm: String,
        scheme: String,
        callback: CefAuthCallback
    ): Boolean {
        val credentials = Ref<Credentials?>()
        val proxyHost = StringUtil.trimTrailing(originUrl, '/')

        if (!GraphicsEnvironment.isHeadless()) {
            val runnable = Runnable {
                val authenticatorDialog = HacProxyAuthenticatorDialog(project, proxyHost, proxyCredentials)
                authenticatorDialog.show()

                credentials.set(authenticatorDialog.credentials)
            }
            if (EventQueue.isDispatchThread()) runnable.run()
            else try {
                EventQueue.invokeAndWait(runnable)
            } catch (e: Throwable) {
                thisLogger().error(e)
            }
        }

        return credentials.get()
            ?.let {
                authorizationRef.set(Credentials(it.userName, it.password))
                callback.Continue(it.userName, it.getPasswordAsString())
                true
            }
            ?: false
    }
}
