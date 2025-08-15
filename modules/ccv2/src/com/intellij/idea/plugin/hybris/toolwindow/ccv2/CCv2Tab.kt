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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.AbstractCCv2DataView
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2BuildsDataView
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2DeploymentsDataView
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2EnvironmentsDataView
import javax.swing.Icon

enum class CCv2Tab(val title: String, val icon: Icon, val view: AbstractCCv2DataView<*>) {
    ENVIRONMENTS("Environments", HybrisIcons.CCv2.ENVIRONMENTS, CCv2EnvironmentsDataView),
    BUILDS("Builds", HybrisIcons.CCv2.BUILDS, CCv2BuildsDataView),
    DEPLOYMENTS("Deployments", HybrisIcons.CCv2.DEPLOYMENTS, CCv2DeploymentsDataView),
}