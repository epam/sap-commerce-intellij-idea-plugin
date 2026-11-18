/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.exec

import com.sun.net.httpserver.HttpServer
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdeProxyTest {

    private val proxyRequests = CopyOnWriteArrayList<String>()
    private val fakeProxy = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange ->
            proxyRequests += exchange.requestURI.toString()
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        start()
    }

    private val httpProxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", fakeProxy.address.port))

    @AfterTest
    fun tearDown() = fakeProxy.stop(0)

    private fun selectorOf(vararg proxies: Proxy) = object : ProxySelector() {
        override fun select(uri: URI?) = proxies.toList()
        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) = Unit
    }

    @Test
    fun `request is routed through the proxy chosen by the selector`() {
        val client = HttpClients.custom().useIdeProxy(selectorOf(httpProxy)).build()

        val statusCode = client.use { it.execute(HttpGet("http://sap-commerce.invalid/hac/console")).statusLine.statusCode }

        assertEquals(200, statusCode)
        assertEquals(listOf("http://sap-commerce.invalid/hac/console"), proxyRequests.toList())
    }

    @Test
    fun `request bypasses the proxy when the selector chooses a direct connection`() {
        val client = HttpClients.custom().useIdeProxy(selectorOf(Proxy.NO_PROXY)).build()

        val direct = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { it.sendResponseHeaders(204, -1); it.close() }
            start()
        }

        val statusCode = client.use { it.execute(HttpGet("http://127.0.0.1:${direct.address.port}/")).statusLine.statusCode }
        direct.stop(0)

        assertEquals(204, statusCode)
        assertTrue(proxyRequests.isEmpty())
    }

    @Test
    fun `ideProxyFor returns the first proxy offered by the selector`() {
        val proxy = ideProxyFor("https://sap-commerce.invalid:9002/hac", selectorOf(httpProxy, Proxy.NO_PROXY))

        assertEquals(httpProxy, proxy)
    }

    @Test
    fun `ideProxyFor returns a direct connection when the selector offers nothing`() {
        val proxy = ideProxyFor("https://sap-commerce.invalid:9002/hac", selectorOf())

        assertEquals(Proxy.NO_PROXY, proxy)
    }

    @Test
    fun `ideProxyFor returns a direct connection when the url is malformed`() {
        val proxy = ideProxyFor("not a valid url", selectorOf(httpProxy))

        assertEquals(Proxy.NO_PROXY, proxy)
    }
}
