/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.gradle;

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings

class GradleSupport {

    fun clearLinkedProjectSettings(project: Project) {
        GradleSettings.getInstance(project).linkedProjectsSettings = emptyList()
    }

    companion object {
        val instance: GradleSupport? = ApplicationManager.getApplication().getService(GradleSupport::class.java)
    }
}