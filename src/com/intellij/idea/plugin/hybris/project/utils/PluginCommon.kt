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

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

object PluginCommon {

    const val JREBEL_PLUGIN_ID = "JRebelPlugin"
    const val ANT_SUPPORT_PLUGIN_ID = "AntSupport"
    const val MAVEN_PLUGIN_ID = "org.jetbrains.idea.maven"
    const val JAVA_PLUGIN_ID = "com.intellij.modules.java"
    const val JAVAEE_PLUGIN_ID = "com.intellij.javaee"
    const val JAVAEE_WEB_PLUGIN_ID = "com.intellij.javaee.web"
    const val JAVAEE_EL_PLUGIN_ID = "com.intellij.javaee.el"
    const val SPRING_PLUGIN_ID = "com.intellij.spring"
    const val KOTLIN_PLUGIN_ID = "org.jetbrains.kotlin"
    const val GROOVY_PLUGIN_ID = "org.intellij.groovy"
    const val GRADLE_PLUGIN_ID = "org.jetbrains.plugins.gradle"
    const val SHOW_UNLINKED_GRADLE_POPUP = "show.inlinked.gradle.project.popup"

    @JvmStatic
    fun isPluginActive(id: String) = PluginManagerCore.getPlugin(PluginId.getId(id))
        ?.isEnabled
        ?: false

}
