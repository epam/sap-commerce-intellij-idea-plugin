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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.view

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.IdeBundle
import com.intellij.idea.ActionsBundle
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.meta.MetaModelChangeListener
import com.intellij.idea.plugin.hybris.system.meta.MetaModelStateService
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.GridBagLayout
import java.io.Serial

class BSView(val myProject: Project) : SimpleToolWindowPanel(false, true), Disposable {

    private val myBeansViewActionGroup: DefaultActionGroup by lazy(::initBeansViewActionGroup)
    private val mySettings = BSViewSettings.getInstance(myProject)
    private val myTreePane = BSTreePanel(myProject)
    private val metaModelStateService = myProject.service<BSMetaModelStateService>()

    override fun dispose() {
        //NOP
    }

    init {
        installToolbar()

        when {
            DumbService.isDumb(myProject) -> with(JBPanel<JBPanel<*>>(GridBagLayout())) {
                add(JBLabel(message("hybris.toolwindow.bs.suspended.text", IdeBundle.message("progress.performing.indexing.tasks"))))
                setContent(this)
            }

            !metaModelStateService.initialized() -> setContentInitializing()

            else -> refreshContent(BSViewSettings.ChangeType.FULL)
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
                    refreshContent(changeType)
                }
            })
            subscribe(MetaModelStateService.TOPIC, object : MetaModelChangeListener {
                override fun beanSystemChanged(globalMetaModel: BSGlobalMetaModel) {
                    refreshContent(globalMetaModel, BSViewSettings.ChangeType.FULL)
                }
            })
        }
    }

    private fun refreshContent(changeType: BSViewSettings.ChangeType) {
        try {
            refreshContent(metaModelStateService.get(), changeType)
        } catch (e: ProcessCanceledException) {
            setContentInitializing()
        }
    }

    private fun refreshContent(globalMetaModel: BSGlobalMetaModel, changeType: BSViewSettings.ChangeType) {
        myTreePane.update(globalMetaModel, changeType)

        if (content != myTreePane) {
            setContent(myTreePane)
        }
    }

    private fun setContentInitializing() {
        with(JBPanel<JBPanel<*>>(GridBagLayout())) {
            add(JBLabel(message("hybris.toolwindow.bs.suspended.text", message("hybris.toolwindow.bs.suspended.initializing.text"))))
            setContent(this)
        }
    }

    private fun initBeansViewActionGroup(): DefaultActionGroup = with(
        DefaultActionGroup(
            message("hybris.toolwindow.action.view_options.text"),
            true
        )
    ) {
        templatePresentation.icon = HybrisIcons.BeanSystem.Preview.SHOW

        addSeparator(ActionsBundle.message("separator.show"))
        add(ShowOnlyCustomAction(mySettings))
        add(ShowOnlyDeprecatedAction(mySettings))
        addSeparator("-- Enum --")
        add(ShowMetaEnumValuesAction(mySettings))
        addSeparator("-- Bean --")
        add(ShowMetaBeanPropertiesAction(mySettings))
        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5943815445616586522L
    }

}
