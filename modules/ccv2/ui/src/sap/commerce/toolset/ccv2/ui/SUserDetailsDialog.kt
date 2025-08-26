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

package sap.commerce.toolset.ccv2.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.SUser

class SUserDetailsDialog(
    private val project: Project,
    private val sUser: SUser,
    private val sUserDto: SUser.Mutable = sUser.mutable()
) : DialogWrapper(project), Disposable {

    private lateinit var idTextField: JBTextField

    init {
        title = "Define an Alias for the S-User"
        super.init()
    }

    override fun dispose() = super.dispose()

    override fun createCenterPanel() = panel {
        row {
            idTextField = textField()
                .label("S-User:")
                .align(AlignX.FILL)
                .enabled(false)
                .component
                .also { it.text = sUserDto.id }
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label("Alias:")
                .align(AlignX.FILL)
                .onChanged { sUserDto.alias = it.text }
                .component
                .also { it.text = sUserDto.alias }
        }.layout(RowLayout.PARENT_GRID)
    }.also {
        it.border = JBUI.Borders.empty(16)
    }

    override fun applyFields() {
        val developerSettings = CCv2DeveloperSettings.getInstance(project)
        val mutableSettings = developerSettings.ccv2Settings.mutable()
        mutableSettings.sUsers[sUser.id] = sUserDto.immutable()

        developerSettings.ccv2Settings = mutableSettings.immutable()
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = idTextField
}