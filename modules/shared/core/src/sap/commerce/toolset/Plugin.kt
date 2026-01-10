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

package sap.commerce.toolset

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.util.application
import org.jetbrains.annotations.Nullable

enum class Plugin(val id: String, val url: String? = null, val dependencies: Set<Plugin> = emptySet()) {

    HYBRIS(HybrisConstants.PLUGIN_ID, url = "https://plugins.jetbrains.com/plugin/12867-sap-commerce-developers-toolset"),
    ULTIMATE("com.intellij.modules.ultimate"),
    JREBEL("JRebelPlugin", url = "https://plugins.jetbrains.com/plugin/4441-jrebel-and-xrebel"),
    ANT_SUPPORT("AntSupport", url = "https://plugins.jetbrains.com/plugin/23025-ant"),
    MAVEN("org.jetbrains.idea.maven"),
    KOTLIN("org.jetbrains.kotlin", url = "https://plugins.jetbrains.com/plugin/6954-kotlin"),
    GROOVY("org.intellij.groovy", url = "https://plugins.jetbrains.com/plugin/1524-groovy"),
    GRADLE("com.intellij.gradle"),
    ECLIPSE("org.jetbrains.idea.eclipse"),
    ANGULAR("AngularJS", url = "https://plugins.jetbrains.com/plugin/6971-angular"),
    DATABASE("com.intellij.database", url = "https://plugins.jetbrains.com/plugin/10925-database-tools-and-sql-for-webstorm", dependencies = setOf(ULTIMATE)),
    DIAGRAM("com.intellij.diagram"),
    PROPERTIES("com.intellij.properties", url = "https://plugins.jetbrains.com/plugin/11594-properties"),
    COPYRIGHT("com.intellij.copyright", url = "https://plugins.jetbrains.com/plugin/13114-copyright"),
    JAVASCRIPT("JavaScript", url = "https://plugins.jetbrains.com/plugin/22069-javascript-and-typescript", dependencies = setOf(ULTIMATE)),
    JAVAEE("com.intellij.javaee", url = "https://plugins.jetbrains.com/plugin/20207-jakarta-ee-platform", dependencies = setOf(ULTIMATE)),
    JAVAEE_WEB("com.intellij.javaee.web", url = "https://plugins.jetbrains.com/plugin/20216-jakarta-ee-web-servlets", dependencies = setOf(ULTIMATE)),
    JAVAEE_EL("com.intellij.javaee.el", url = "https://plugins.jetbrains.com/plugin/20208-jakarta-ee-expression-language-el-", dependencies = setOf(ULTIMATE)),
    SPRING("com.intellij.spring", url = "https://plugins.jetbrains.com/plugin/20221-spring", dependencies = setOf(ULTIMATE)),
    CRON("com.intellij.cron", url = "https://plugins.jetbrains.com/plugin/24438-cron-expressions", dependencies = setOf(ULTIMATE)),
    GRID("intellij.grid.plugin"),
    JAVA_I18N("com.intellij.java-i18n");

    val pluginId: PluginId
        get() = PluginId.getId(id)

    val pluginDescriptor: IdeaPluginDescriptor?
        @Nullable
        get() = PluginManagerCore.getPlugin(pluginId)

    fun isActive() = isActive(pluginId) && dependencies.all { isActive(it.pluginId) }

    private fun isActive(pluginId: PluginId): Boolean = PluginManagerCore.isLoaded(pluginId) && !PluginManagerCore.isDisabled(pluginId)

    fun <T> ifActive(operation: () -> T): T? = if (isActive()) operation() else null

    fun <T> ifDisabled(operation: () -> T): T? = if (isDisabled()) operation() else null

    fun isDisabled() = !isActive()

    fun <T> service(clazz: Class<T>): T? = ifActive { application.getService(clazz) }
    fun <T> service(project: Project, clazz: Class<T>): T? = ifActive { project.getService(clazz) }

    companion object {
        fun of(id: String) = Plugin.entries.find { it.id == id }
    }
}