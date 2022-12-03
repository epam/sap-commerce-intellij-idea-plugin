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

package com.intellij.idea.plugin.hybris.toolwindow.beans.view

import com.intellij.idea.plugin.hybris.toolwindow.beans.components.BeansTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer

class BeansView(val myProject: Project) : SimpleToolWindowPanel(false, true), Disposable {

    private val myItemsViewActionGroup: DefaultActionGroup by lazy(::initItemsViewActionGroup)
    private val mySettings = BeansViewSettings.getInstance(myProject)
    private val myTreePane = BeansTreePanel(myProject)

    override fun dispose() {
        //NOP
    }

    init {
        setContent(myTreePane);

        Disposer.register(this, myTreePane)

        installToolbar()
        installSettingsListener()
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(myItemsViewActionGroup)
            ActionManager.getInstance().createActionToolbar("HybrisBeansView", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installSettingsListener() {
        myProject.messageBus.connect(this).subscribe(BeansViewSettings.TOPIC, object : BeansViewSettings.Listener {
            override fun settingsChanged(changeType: BeansViewSettings.ChangeType) {
                myTreePane.update(changeType)
            }
        })
    }

    private fun initItemsViewActionGroup(): DefaultActionGroup = with(DefaultActionGroup()) {
        add(ShowOnlyCustomAction(mySettings))
        addSeparator()
        this
    }

}
