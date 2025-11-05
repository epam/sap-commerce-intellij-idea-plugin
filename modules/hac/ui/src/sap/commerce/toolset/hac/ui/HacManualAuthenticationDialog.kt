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
import com.intellij.openapi.util.Ref
import com.intellij.ui.jcef.*
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import org.cef.handler.CefLoadHandler
import sap.commerce.toolset.hac.auth.HacAuthenticationContext
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
    private val deferredAuthenticationContext: CompletableDeferred<HacAuthenticationContext?>,
) : DialogWrapper(project, null, false, IdeModalityType.MODELESS) {

    private val authorizationRef = Ref<Credentials?>()

    private val jbCefBrowser = JBCefBrowser.createBuilder()
        .setOffScreenRendering(JBCefApp.isOffScreenRenderingModeEnabled())
        .setUrl(settings.generatedURL)
        .setCreateImmediately(true)
        .setEnableOpenDevToolsMenuItem(true)
        .build()
        .let { it as JBCefBrowserBase }
        .apply {
            Disposer.register(disposable, this)

            setProperty(JBCefBrowser.Properties.FOCUS_ON_SHOW, true)
            jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 20)

            setErrorPage { errorCode, errorText, failedUrl ->
                if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null
                else JBCefBrowserBase.ErrorPage.DEFAULT.create(errorCode, errorText, failedUrl)
            }

            if (settings.proxyAuthentication) {
                jbCefClient.addRequestHandler(
                    ProxyAuthCefRequestHandlerAdapter(project, proxyCredentials, authorizationRef),
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

        val csrf = runBlocking { retrieveCsrfToken() }
        val authorization = authorizationRef.get()
        val context = HacAuthenticationContext(csrf, cookies, authorization)

        deferredAuthenticationContext.complete(context)
    }

    override fun doCancelAction() {
        super.doCancelAction()
        deferredAuthenticationContext.complete(null)
    }

    // reference -> JcefComponentWrapper
    suspend fun retrieveCsrfToken(executeTimeoutMs: Long = 3000): String = withTimeout(executeTimeoutMs) {
        suspendCancellableCoroutine { continuation ->
            val jsQuery = JBCefJSQuery.create(jbCefBrowser)
            val script = """
                (() => {
                const v = document.querySelector('meta[name="_csrf"]')?.content ?? "";
                ${jsQuery.inject("v")}
            })();
            """.trimIndent()

            coroutineContext.job.invokeOnCompletion { Disposer.dispose(jsQuery) }

            lateinit var handler: (String) -> JBCefJSQuery.Response?
            handler = { csrfString ->
                jsQuery.removeHandler(handler)
                continuation.resumeWith(Result.success(csrfString))
                JBCefJSQuery.Response(csrfString)
            }
            jsQuery.addHandler(handler)

            continuation.invokeOnCancellation { jsQuery.removeHandler(handler) }

            jbCefBrowser.cefBrowser.executeJavaScript(script, null, 0)
        }
    }
}