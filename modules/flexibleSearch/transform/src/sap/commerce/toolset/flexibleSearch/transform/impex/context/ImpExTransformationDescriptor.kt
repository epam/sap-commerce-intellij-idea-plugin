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

package sap.commerce.toolset.flexibleSearch.transform.impex.context

import com.intellij.openapi.project.Project
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.transform.context.FxSQueryInfo
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

data class ImpExTransformationDescriptor(
    val project: Project,
    val queryInfo: FxSQueryInfo,
    val params: List<ImpExHeaderParameter>,
    val joinUniqueParams: List<ImpExHeaderParameter>,
    val rows: List<List<String>>,
    val connection: HacConnectionSettingsState? = null,
    val execSettings: FlexibleSearchExecContext.Settings,
) {
    val typeName: String
        get() = queryInfo.primaryType
}