/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.view

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.ShowModulesAction
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.panels.HybrisTypeSystemPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.FinderRecursivePanel


class HybrisTypeSystemView(val project: Project) : SimpleToolWindowPanel(false, true), Disposable {

    var myProject: Project
    var myRootPanel: FinderRecursivePanel<*>
    val myStructureViewActionGroup: DefaultActionGroup by lazy(::initStructureViewActionGroup)
    val mySettings: HybrisTypeSystemViewSettings

    override fun dispose() {
        //NOP
    }

    init {
        myProject = project
        mySettings = HybrisTypeSystemViewSettings(myProject)
        myRootPanel = HybrisTypeSystemPanel(myProject, myStructureViewActionGroup)
        myRootPanel.initPanel()
        setContent(myRootPanel)
        Disposer.register(this, myRootPanel)

        installToolbar()
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(myStructureViewActionGroup)
            addSeparator()
            ActionManager.getInstance().createActionToolbar("HybrisTypeSystemView", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun initStructureViewActionGroup(): DefaultActionGroup = with(DefaultActionGroup()) {
        add(ShowModulesAction(mySettings))
        this
    }

}
