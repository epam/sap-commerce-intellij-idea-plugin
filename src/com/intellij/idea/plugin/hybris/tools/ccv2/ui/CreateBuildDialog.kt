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

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI

class CreateBuildDialog(
    private val project: Project
) : DialogWrapper(project) {

    init {
        title = "Schedule New CCv2 Build"
        super.init()
    }

    private lateinit var subscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var nameTextField: JBTextField
    private lateinit var branchTextField: JBTextField

    override fun createCenterPanel() = panel {
        row {
            subscriptionComboBox = comboBox(CCv2SubscriptionsComboBoxModel(project))
                .label("Subscription:")
                .align(AlignX.FILL)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            nameTextField = textField()
                .label("Name:")
                .align(AlignX.FILL)
                .addValidationRule("Please specify name for a build.") { it.text.isBlank() }
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            branchTextField = textField()
                .label("Branch:")
                .align(AlignX.FILL)
                .addValidationRule("Please specify a branch for a build.") { it.text.isBlank() }
                .component
        }.layout(RowLayout.PARENT_GRID)
    }.also {
        it.border = JBUI.Borders.empty(16)
    }

    override fun applyFields() {
        val subscription = subscriptionComboBox.selectedItem as CCv2Subscription
        val name = nameTextField.text!!
        val branch = branchTextField.text!!

        val task = object : Task.Backgroundable(project, "Creating new CCv2 Build...") {
            override fun run(indicator: ProgressIndicator) {
                CCv2Service.getInstance(project).createBuild(subscription, name, branch)
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = subscriptionComboBox
}