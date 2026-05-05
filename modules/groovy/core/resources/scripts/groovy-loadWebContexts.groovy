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

package scripts


import org.apache.catalina.startup.Bootstrap
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.FrameworkServlet

static Map<String,WebApplicationContext> getSpringWeb() {
    Map<String,WebApplicationContext> map = [:]
    def server = Bootstrap.daemon.catalinaDaemon.server
    server.findServices().each { service ->
        service.container.findChildren().each { host ->
            host.findChildren().each { context ->
                def contextKey = []
                if (service.name != 'Catalina') contextKey << service.name
                if (host.name != service.container.defaultHost) contextKey << host.name
                contextKey << context.baseName

                // spring mvc servlets:
                def mvcServletWrappers = context.findChildren().findAll { it.servlet instanceof FrameworkServlet }
                def rootWebContext = WebApplicationContextUtils.getWebApplicationContext(context.servletContext)

                if (mvcServletWrappers) {
                    mvcServletWrappers.each { mvcServletWrapper ->
                        def attributeName = mvcServletWrapper.servlet.servletContextAttributeName
                        def springContextKey = []
                        if (mvcServletWrappers.size() > 1) {
                            springContextKey = [attributeName - FrameworkServlet.SERVLET_CONTEXT_PREFIX]
                        }
                        map << [('/' + (contextKey + springContextKey).join('/')):context.servletContext.getAttribute(attributeName)]
                    }
                } else if (rootWebContext) {
                    map << [('/' + contextKey.join('/')):rootWebContext]
                }
            }
        }
    }
    map
}

getSpringWeb().keySet().join("|")
