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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.*
import javax.swing.Icon

enum class CCv2Tab(val title: String, val icon: Icon, val view: AbstractCCv2DataView<*>) {
    ENVIRONMENTS("Environments", HybrisIcons.CCV2_ENVIRONMENTS, CCv2EnvironmentsDataView),
    BUILDS("Builds", HybrisIcons.CCV2_BUILDS, CCv2BuildsDataView),
    DEPLOYMENTS("Deployments", HybrisIcons.CCV2_DEPLOYMENTS, CCv2DeploymentsDataView),
    BACKUPS("Backups", HybrisIcons.CCV2_BACKUPS, CCv2BackupsDataView),
    ENDPOINTS("Endpoints", HybrisIcons.CCV2_ENDPOINTS, CCv2EndpointsDataView),
}