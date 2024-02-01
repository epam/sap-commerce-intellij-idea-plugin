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
package com.intellij.idea.plugin.hybris.project.utils

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.extensions.PluginId

object PluginCommon {

    val PLUGIN_ID_JREBEL = Plugin("JRebelPlugin", "https://plugins.jetbrains.com/plugin/4441-jrebel-and-xrebel")
    val PLUGIN_ANT_SUPPORT = Plugin("AntSupport", "https://plugins.jetbrains.com/plugin/23025-ant")
    val PLUGIN_MAVEN = Plugin("org.jetbrains.idea.maven")
    @JvmStatic
    val PLUGIN_JAVAEE = Plugin("com.intellij.javaee", "https://plugins.jetbrains.com/plugin/20207-jakarta-ee-platform")
    @JvmStatic
    val PLUGIN_JAVAEE_WEB = Plugin("com.intellij.javaee.web", "https://plugins.jetbrains.com/plugin/20216-jakarta-ee-web-servlets")
    @JvmStatic
    val PLUGIN_JAVAEE_EL = Plugin("com.intellij.javaee.el", "https://plugins.jetbrains.com/plugin/20208-jakarta-ee-expression-language-el-")
    @JvmStatic
    val PLUGIN_SPRING = Plugin("com.intellij.spring", "https://plugins.jetbrains.com/plugin/20221-spring")
    val PLUGIN_KOTLIN = Plugin("org.jetbrains.kotlin", "https://plugins.jetbrains.com/plugin/6954-kotlin")
    val PLUGIN_GROOVY = Plugin("org.intellij.groovy", "https://plugins.jetbrains.com/plugin/1524-groovy")
    val PLUGIN_GRADLE = Plugin("org.jetbrains.plugins.gradle")
    val PLUGIN_DATABASE = Plugin("com.intellij.database", "https://plugins.jetbrains.com/plugin/10925-database-tools-and-sql-for-webstorm")
    val PLUGIN_DIAGRAM = Plugin("com.intellij.diagram")
    val PLUGIN_PROPERTIES = Plugin("com.intellij.properties", "https://plugins.jetbrains.com/plugin/11594-properties")
    val PLUGIN_COPYRIGHT = Plugin("com.intellij.copyright", "https://plugins.jetbrains.com/plugin/13114-copyright")
    const val SHOW_UNLINKED_GRADLE_POPUP = "show.inlinked.gradle.project.popup"

    @JvmStatic
    fun isPluginActive(plugin: Plugin) = PluginManagerCore.getPlugin(PluginId.getId(plugin.id))
        ?.isEnabled
        ?: false

    fun enablePlugins(pluginIds: Collection<PluginId>) {
        val pluginManager = PluginManager.getInstance()
        pluginIds.forEach { pluginManager.enablePlugin(it) }

        ApplicationManager.getApplication()
            .let { it as? ApplicationEx }
            ?.restart(true)
    }
}
