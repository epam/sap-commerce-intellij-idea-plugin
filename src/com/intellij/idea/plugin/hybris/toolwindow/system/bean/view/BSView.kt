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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.view

import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.bean.meta.BSChangeListener
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelAccessImpl
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.GridBagLayout

class BSView(val myProject: Project) : SimpleToolWindowPanel(false, true), Disposable {

    private val myBeansViewActionGroup: DefaultActionGroup by lazy(::initBeansViewActionGroup)
    private val mySettings = BSViewSettings.getInstance(myProject)
    private val myTreePane = BSTreePanel(myProject)

    override fun dispose() {
        //NOP
    }

    init {
        installToolbar()

        if (DumbService.isDumb(myProject)) {
            val panel = JBPanel<JBPanel<*>>(GridBagLayout())
            panel.add(JBLabel(message("hybris.toolwindow.bs.suspended.text", IdeBundle.message("progress.performing.indexing.tasks"))))
            setContent(panel)
        } else {
            refreshContent()
        }

        Disposer.register(this, myTreePane)
        installSettingsListener()
    }

    private fun installToolbar() {
        val actionsManager = CommonActionsManager.getInstance()
        val toolbar = with(DefaultActionGroup()) {
            add(myBeansViewActionGroup)
            addSeparator()
            add(actionsManager.createExpandAllHeaderAction(myTreePane.tree))
            add(actionsManager.createCollapseAllHeaderAction(myTreePane.tree))
            ActionManager.getInstance().createActionToolbar("HybrisBSView", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installSettingsListener() {
        with(myProject.messageBus.connect(this)) {
            subscribe(BSViewSettings.TOPIC, object : BSViewSettings.Listener {
                override fun settingsChanged(changeType: BSViewSettings.ChangeType) {
                    myTreePane.update(changeType)
                }
            })
            subscribe(BSMetaModelAccessImpl.topic, object : BSChangeListener {
                override fun beanSystemChanged(globalMetaModel: BSGlobalMetaModel) {
                    refreshContent()
                }
            })
        }
    }

    private fun refreshContent() {
        myTreePane.update(BSViewSettings.ChangeType.FULL)

        if (content != myTreePane) {
            setContent(myTreePane)
        }
    }

    private fun initBeansViewActionGroup(): DefaultActionGroup = with(
        DefaultActionGroup(
            message("hybris.toolwindow.action.view_options.text"),
            true
        )
    ) {
        templatePresentation.icon = AllIcons.Actions.Show

        addSeparator(message("hybris.toolwindow.action.separator.show"))
        add(ShowOnlyCustomAction(mySettings))
        add(ShowOnlyDeprecatedAction(mySettings))
        addSeparator("-- Enum --")
        add(ShowMetaEnumValuesAction(mySettings))
        addSeparator("-- Bean --")
        add(ShowMetaBeanPropertiesAction(mySettings))
        this
    }

    companion object {
        private const val serialVersionUID: Long = 5943815445616586522L
    }

}
