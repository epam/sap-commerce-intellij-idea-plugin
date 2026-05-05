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

package sap.commerce.toolset.groovy

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.settings.DeveloperSettings
import sap.commerce.toolset.settings.state.SpringContextMode

fun VirtualFile?.getCurrentSpringContextMode(project: Project?) = this
    ?.getUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE)
    ?: project?.let { DeveloperSettings.getInstance(project).groovySettings.springContextMode }
    ?: SpringContextMode.DISABLED


var VirtualFile.groovyWebContexts
    get() = this.getUserData(GroovyExecContext.KEY_WEB_CONTEXTS)
    set(value) {
        this.putUserData(GroovyExecContext.KEY_WEB_CONTEXTS, value)
    }

var VirtualFile.groovyWebContextsFetching
    get() = this.getUserData(GroovyExecContext.KEY_WEB_CONTEXTS_FETCHING) ?: false
    set(value) {
        this.putUserData(GroovyExecContext.KEY_WEB_CONTEXTS_FETCHING, value)
    }

var VirtualFile.groovyExecContextSettings
    get() = this.getUserData(GroovyExecContext.KEY_EXECUTION_SETTINGS)
    set(value) {
        this.putUserData(GroovyExecContext.KEY_EXECUTION_SETTINGS, value)
    }

fun VirtualFile.groovyExecContextSettings(fallback: () -> GroovyExecContext.Settings) = this
    .getUserData(GroovyExecContext.KEY_EXECUTION_SETTINGS)
    ?: fallback()
