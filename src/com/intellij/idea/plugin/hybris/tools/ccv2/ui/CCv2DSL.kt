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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui

import com.intellij.ide.HelpTooltip
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import javax.swing.Icon

object CCv2DSL {

    fun Row.sUser(project: Project, sUserId: String, icon: Icon, label: String = "Created by") {
        icon(icon)
            .gap(RightGap.SMALL)
        val sUser = DeveloperSettingsComponent.getInstance(project).getSUser(sUserId)
        link(sUser.toString()) { SUserDetailsDialog(project, sUser).showAndGet() }
            .comment(label)
            .applyToComponent {
                HelpTooltip()
                    .setTitle("Define an alias for the S-User")
                    .installOn(this);
            }
    }
}