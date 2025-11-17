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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ExitActionType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Ref
import com.intellij.ui.jcef.*
import com.intellij.util.io.await
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import org.cef.handler.CefLoadHandler
import sap.commerce.toolset.hac.auth.HacManualAuthContext
import sap.commerce.toolset.hac.auth.ProxyAuthCefRequestHandlerAdapter
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.exec.settings.state.ProxyAuthMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
    It is mandatory to use MODELESS Modality type to ensure correct render of the Cef Browser
 */
class HacManualAuthenticationDialog(
    private val project: Project,
    private val settings: HacConnectionSettingsState,
    private val proxyCredentials: Credentials? = null,
    private val deferredContext: CompletableDeferred<HacManualAuthContext?>,
) : DialogWrapper(project, null, false, IdeModalityType.MODELESS) {

    private val proxyAuthRef = Ref<Credentials?>()

    private val jbCefBrowser = JBCefBrowser.createBuilder()
        .setOffScreenRendering(JBCefApp.isOffScreenRenderingModeEnabled())
        .setUrl(settings.generatedURL)
        .setCreateImmediately(true)
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

            if (settings.proxyAuthMode == ProxyAuthMode.BASIC) {
                jbCefClient.addRequestHandler(
                    ProxyAuthCefRequestHandlerAdapter(project, proxyCredentials, proxyAuthRef),
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

    override fun doOKAction() {
        CoroutineScope(Dispatchers.EDT).launch {
            okAction.isEnabled = false

            val cookiesFuture = jbCefBrowser.getJBCefCookieManager()
                .getCookies(null, false)

            val cookies =
                try {
                    withTimeout(3.seconds) {
                        cookiesFuture
                            .await()
                            .associate { it.name to it.value }
                    }
                } catch (e: Exception) {
                    thisLogger().debug(e)
                    emptyMap()
                }

            val csrfToken = retrieveCsrfToken()
            val proxyCredentials = proxyAuthRef.get()
            val context = HacManualAuthContext(csrfToken, cookies, proxyCredentials)

            deferredContext.complete(context)
            close(OK_EXIT_CODE, ExitActionType.OK)
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
        deferredContext.complete(null)
    }

    // reference -> JcefComponentWrapper
    suspend fun retrieveCsrfToken(timeout: Duration = 3.seconds): String = withTimeout(timeout) {
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