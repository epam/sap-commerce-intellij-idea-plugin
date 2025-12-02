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

package sap.commerce.toolset.logging.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.logging.state.CxCustomLoggerTemplateState
import sap.commerce.toolset.logging.state.CxCustomLoggerTemplates

@State(
    name = "[y] Custom Loggers Templates",
    storages = [Storage(HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.LOCAL)]
)
@Service(Service.Level.PROJECT)
class CxLoggerTemplatesSettings : SerializablePersistentStateComponent<CxCustomLoggerTemplates>(CxCustomLoggerTemplates()), ModificationTracker {
    var customLoggerTemplates: List<CxCustomLoggerTemplateState>
        get() = state.customLoggerTemplates
        set(value) {
            updateState { it.copy(customLoggerTemplates = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CxLoggerTemplatesSettings = project.service()
    }
}