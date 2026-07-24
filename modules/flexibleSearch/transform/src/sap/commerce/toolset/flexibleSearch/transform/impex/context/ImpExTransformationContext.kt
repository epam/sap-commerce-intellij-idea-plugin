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

import sap.commerce.toolset.flexibleSearch.transform.context.FkResolutionInfo
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

/**
 * Enriched transformation context built from a [ImpExTransformationDescriptor] after the
 * enum and FK resolution maps have been computed.
 *
 * Passed to [sap.commerce.toolset.flexibleSearch.transform.impex.ImpExTransformationService.resolveAndBuild]
 * so that method receives everything it needs without individual parameters.
 */
data class ImpExTransformationContext(
    val descriptor: ImpExTransformationDescriptor,
    val connection: HacConnectionSettingsState,
    val enumSourceIndicesByType: Map<Int, String>,
    val fkSourceIndicesByResolutionInfo: Map<Int, FkResolutionInfo>,
)