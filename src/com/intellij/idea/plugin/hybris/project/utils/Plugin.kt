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

package com.intellij.idea.plugin.hybris.project.utils

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.extensions.PluginId
import javax.annotation.Nullable

enum class Plugin(val id: String, val url: String? = null) {

    HYBRIS(HybrisConstants.PLUGIN_ID, "https://plugins.jetbrains.com/plugin/12867-sap-commerce-developers-toolset"),
    JREBEL("JRebelPlugin", "https://plugins.jetbrains.com/plugin/4441-jrebel-and-xrebel"),
    ANT_SUPPORT("AntSupport", "https://plugins.jetbrains.com/plugin/23025-ant"),
    MAVEN("org.jetbrains.idea.maven"),
    KOTLIN("org.jetbrains.kotlin", "https://plugins.jetbrains.com/plugin/6954-kotlin"),
    GROOVY("org.intellij.groovy", "https://plugins.jetbrains.com/plugin/1524-groovy"),
    GRADLE("com.intellij.gradle"),
    ANGULAR("AngularJS", "https://plugins.jetbrains.com/plugin/6971-angular"),
    DATABASE("com.intellij.database", "https://plugins.jetbrains.com/plugin/10925-database-tools-and-sql-for-webstorm"),
    DIAGRAM("com.intellij.diagram"),
    PROPERTIES("com.intellij.properties", "https://plugins.jetbrains.com/plugin/11594-properties"),
    COPYRIGHT("com.intellij.copyright", "https://plugins.jetbrains.com/plugin/13114-copyright"),
    JAVASCRIPT("JavaScript", "https://plugins.jetbrains.com/plugin/22069-javascript-and-typescript"),
    INTELLILANG("org.intellij.intelliLang", "https://plugins.jetbrains.com/plugin/13374-intellilang"),
    JAVAEE("com.intellij.javaee", "https://plugins.jetbrains.com/plugin/20207-jakarta-ee-platform"),
    JAVAEE_WEB("com.intellij.javaee.web", "https://plugins.jetbrains.com/plugin/20216-jakarta-ee-web-servlets"),
    JAVAEE_EL("com.intellij.javaee.el", "https://plugins.jetbrains.com/plugin/20208-jakarta-ee-expression-language-el-"),
    SPRING("com.intellij.spring", "https://plugins.jetbrains.com/plugin/20221-spring"),
    CRON("com.intellij.cron", "https://plugins.jetbrains.com/plugin/24438-cron-expressions"),
    GRID("intellij.grid.plugin");

    val pluginId: PluginId
        get() = PluginId.getId(id)

    @Nullable
    val pluginDescriptor: IdeaPluginDescriptor?
        get() = PluginManagerCore.getPlugin(pluginId)

    fun isActive() = pluginDescriptor
        ?.isEnabled
        ?: false

    fun <T> ifActive(operation: () -> T): T? = if (isActive()) operation() else null

    fun <T> ifDisabled(operation: () -> T): T? = if (isDisabled()) operation() else null

    fun isDisabled() = !isActive()
}
