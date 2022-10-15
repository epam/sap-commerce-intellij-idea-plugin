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

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.components.TSTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer

class TSView(val project: Project) : SimpleToolWindowPanel(false, true), Disposable {

    var myProject: Project
    val myItemsViewActionGroup: DefaultActionGroup by lazy(::initItemsViewActionGroup)
    val mySettings: TSViewSettings

    override fun dispose() {
        //NOP
    }

    init {
        myProject = project
        mySettings = TSViewSettings(myProject)
        val pane = TSTreePanel(myProject)
        setContent(pane);

        Disposer.register(this, pane)

        installToolbar()
        installSettingsListener()
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(myItemsViewActionGroup)
            ActionManager.getInstance().createActionToolbar("HybrisTypeSystemView", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installSettingsListener() {
//        myProject.messageBus.connect(this).subscribe(TSViewSettings.TOPIC,
//            TSViewSettings.Listener { changeType ->
//                if (changeType == SpringBeansViewSettings.ChangeType.FULL) {
//                    this@SpringBeansView.performFullUpdate()
//                } else if (changeType == SpringBeansViewSettings.ChangeType.UPDATE_DETAILS) {
//                    this@SpringBeansView.performDetailsUpdate()
//                } else if (changeType == SpringBeansViewSettings.ChangeType.UPDATE_LIST) {
//                    this@SpringBeansView.myRootPanel.updatePanel()
//                } else if (changeType == SpringBeansViewSettings.ChangeType.FORCE_UPDATE_RIGHT_COMPONENT) {
//                    this@SpringBeansView.myRootPanel.updateRightComponent(true)
//                }
//            })
    }

    private fun initItemsViewActionGroup(): DefaultActionGroup = with(DefaultActionGroup()) {
        add(ShowOnlyCustomAction(mySettings))
        addSeparator()
//        add(ShowMetaItemsAction(mySettings))
//        add(ShowMetaEnumsAction(mySettings))
//        add(ShowMetaCollectionsAction(mySettings))
//        add(ShowMetaMapsAction(mySettings))
//        add(ShowMetaRelationsAction(mySettings))
//        add(ShowMetaAtomicsAction(mySettings))
        this
    }

    companion object {
        private const val serialVersionUID: Long = 74100584202830949L
    }

}
