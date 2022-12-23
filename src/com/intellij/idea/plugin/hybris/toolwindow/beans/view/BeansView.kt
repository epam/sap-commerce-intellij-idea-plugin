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

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.toolwindow.beans.components.BeansTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.GridBagLayout
import java.awt.GridLayout

class BeansView(val myProject: Project) : SimpleToolWindowPanel(false, true), Disposable {

    private val myBeansViewActionGroup: DefaultActionGroup by lazy(::initBeansViewActionGroup)
    private val mySettings = BeansViewSettings.getInstance(myProject)
    private val myTreePane = BeansTreePanel(myProject)

    override fun dispose() {
        //NOP
    }

    init {
        installToolbar()

        if (DumbService.isDumb(myProject)) {
            val panel = JBPanel<JBPanel<*>>(GridBagLayout())
            panel.add(JBLabel(message("hybris.toolwindow.beans.suspended.text", IdeBundle.message("progress.performing.indexing.tasks"))))
            setContent(panel)
        }

        DumbService.getInstance(myProject).runWhenSmart {
            setContent(myTreePane);

            Disposer.register(this, myTreePane)

            installSettingsListener()
        }
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(myBeansViewActionGroup)
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
        this
    }

    companion object {
        private const val serialVersionUID: Long = 5943815445616586522L
    }

}
