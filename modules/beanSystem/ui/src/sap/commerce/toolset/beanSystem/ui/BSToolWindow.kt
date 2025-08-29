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

package sap.commerce.toolset.beanSystem.ui

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.IdeBundle
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.actionSystem.BSShowMetaBeanPropertiesAction
import sap.commerce.toolset.beanSystem.actionSystem.BSShowMetaEnumValuesAction
import sap.commerce.toolset.beanSystem.actionSystem.BSShowOnlyCustomAction
import sap.commerce.toolset.beanSystem.actionSystem.BSShowOnlyDeprecatedAction
import sap.commerce.toolset.beanSystem.meta.BSGlobalMetaModel
import sap.commerce.toolset.beanSystem.meta.BSMetaModelStateService
import sap.commerce.toolset.beanSystem.meta.event.BSMetaModelChangeListener
import sap.commerce.toolset.beanSystem.settings.BSViewSettings
import sap.commerce.toolset.beanSystem.settings.event.BSViewSettingsListener
import sap.commerce.toolset.beanSystem.settings.state.ChangeType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ui.toolwindow.ContentActivationAware
import java.awt.GridBagLayout
import java.io.Serial

class BSToolWindow(private val project: Project) : SimpleToolWindowPanel(false, true), ContentActivationAware, Disposable {

    private val myBeansViewActionGroup: DefaultActionGroup by lazy(::initBeansViewActionGroup)
    private val mySettings = BSViewSettings.getInstance(project)
    private val myTreePane = BSTreePanel(project)

    override fun dispose() {
        //NOP
    }

    init {
        installToolbar()

        when {
            DumbService.isDumb(project) -> with(JBPanel<JBPanel<*>>(GridBagLayout())) {
                add(JBLabel(i18n("hybris.toolwindow.bs.suspended.text", IdeBundle.message("progress.performing.indexing.tasks"))))
                setContent(this)
            }

            !BSMetaModelStateService.getInstance(project).initialized() -> setContentInitializing()

            else -> refreshContent(ChangeType.FULL)
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
        with(project.messageBus.connect(this)) {
            subscribe(BSViewSettingsListener.TOPIC, object : BSViewSettingsListener {
                override fun settingsChanged(changeType: ChangeType) {
                    refreshContent(changeType)
                }
            })
            subscribe(BSMetaModelChangeListener.TOPIC, object : BSMetaModelChangeListener {
                override fun onChanged(globalMetaModel: BSGlobalMetaModel) {
                    refreshContent(globalMetaModel, ChangeType.FULL)
                }
            })
        }
    }

    private fun refreshContent(changeType: ChangeType) {
        try {
            refreshContent(BSMetaModelStateService.state(project), changeType)
        } catch (_: Throwable) {
            setContentInitializing()
        }
    }

    private fun refreshContent(globalMetaModel: BSGlobalMetaModel, changeType: ChangeType) {
        myTreePane.update(globalMetaModel, changeType)

        if (content != myTreePane) {
            setContent(myTreePane)
        }
    }

    private fun setContentInitializing() {
        with(JBPanel<JBPanel<*>>(GridBagLayout())) {
            add(JBLabel(i18n("hybris.toolwindow.bs.suspended.text", i18n("hybris.toolwindow.bs.suspended.initializing.text"))))
            setContent(this)
        }
    }

    private fun initBeansViewActionGroup(): DefaultActionGroup = with(
        DefaultActionGroup(
            i18n("hybris.toolwindow.action.view_options.text"),
            true
        )
    ) {
        templatePresentation.icon = HybrisIcons.BeanSystem.Preview.SHOW

        addSeparator(ActionsBundle.message("separator.show"))
        add(BSShowOnlyCustomAction(mySettings))
        add(BSShowOnlyDeprecatedAction(mySettings))
        addSeparator("-- Enum --")
        add(BSShowMetaEnumValuesAction(mySettings))
        addSeparator("-- Bean --")
        add(BSShowMetaBeanPropertiesAction(mySettings))
        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5943815445616586522L

        const val ID = "Bean System"
    }

}