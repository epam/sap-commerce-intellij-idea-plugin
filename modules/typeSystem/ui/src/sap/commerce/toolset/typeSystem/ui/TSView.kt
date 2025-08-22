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

package sap.commerce.toolset.typeSystem.ui

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
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.actionSystem.*
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.typeSystem.settings.TSViewSettings
import sap.commerce.toolset.typeSystem.settings.event.TSViewSettingsListener
import sap.commerce.toolset.typeSystem.settings.state.ChangeType
import sap.commerce.toolset.typeSystem.ui.components.TSTreePanel
import java.awt.GridBagLayout
import java.io.Serial

class TSView(private val project: Project) : SimpleToolWindowPanel(false, true), Disposable {

    private val myItemsViewActionGroup: DefaultActionGroup by lazy(::initItemsViewActionGroup)
    private val mySettings = TSViewSettings.getInstance(project)
    private val myTreePane = TSTreePanel(project)

    override fun dispose() {
        //NOP
    }

    init {
        installToolbar()

        when {
            DumbService.isDumb(project) -> with(JBPanel<JBPanel<*>>(GridBagLayout())) {
                add(JBLabel(i18n("hybris.toolwindow.ts.suspended.text", IdeBundle.message("progress.performing.indexing.tasks"))))
                setContent(this)
            }

            !TSMetaModelStateService.getInstance(project).initialized() -> setContentInitializing()

            else -> refreshContent(ChangeType.FULL)
        }

        Disposer.register(this, myTreePane)
        installSettingsListener()
    }

    private fun installToolbar() {
        val actionsManager = CommonActionsManager.getInstance()
        val toolbar = with(DefaultActionGroup()) {
            add(myItemsViewActionGroup)
            addSeparator()
            add(actionsManager.createExpandAllHeaderAction(myTreePane.tree))
            add(actionsManager.createCollapseAllHeaderAction(myTreePane.tree))
            ActionManager.getInstance().createActionToolbar("HybrisTSView", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installSettingsListener() {
        with(project.messageBus.connect(this)) {
            subscribe(TSViewSettingsListener.TOPIC, object : TSViewSettingsListener {
                override fun settingsChanged(changeType: ChangeType) {
                    refreshContent(changeType)
                }
            })
            subscribe(TSMetaModelChangeListener.TOPIC, object : TSMetaModelChangeListener {
                override fun onChanged(globalMetaModel: TSGlobalMetaModel) {
                    refreshContent(globalMetaModel, ChangeType.FULL)
                }
            })
        }
    }

    private fun refreshContent(changeType: ChangeType) {
        try {
            refreshContent(TSMetaModelStateService.state(project), changeType)
        } catch (_: Throwable) {
            setContentInitializing()
        }
    }

    private fun refreshContent(globalMetaModel: TSGlobalMetaModel, changeType: ChangeType) {
        myTreePane.update(globalMetaModel, changeType)

        if (content != myTreePane) {
            setContent(myTreePane)
        }
    }

    private fun setContentInitializing() {
        with(JBPanel<JBPanel<*>>(GridBagLayout())) {
            add(JBLabel(i18n("hybris.toolwindow.ts.suspended.text", i18n("hybris.toolwindow.ts.suspended.initializing.text"))))
            setContent(this)
        }
    }

    private fun initItemsViewActionGroup(): DefaultActionGroup = with(
        DefaultActionGroup(
            i18n("hybris.toolwindow.action.view_options.text"),
            true
        )
    ) {
        templatePresentation.icon = HybrisIcons.TypeSystem.Preview.Actions.SHOW

        addSeparator(ActionsBundle.message("separator.show"))
        add(TSShowOnlyCustomAction(mySettings))
        addSeparator("-- Types --")
        add(TSShowMetaAtomicsAction(mySettings))
        add(TSShowMetaEnumsAction(mySettings))
        add(TSShowMetaCollectionsAction(mySettings))
        add(TSShowMetaMapsAction(mySettings))
        add(TSShowMetaRelationsAction(mySettings))
        add(TSShowMetaItemsAction(mySettings))
        addSeparator("-- Enum --")
        add(TSShowMetaEnumValuesAction(mySettings))
        addSeparator("-- Item --")
        add(TSShowMetaItemIndexesAction(mySettings))
        add(TSShowMetaItemAttributesAction(mySettings))
        add(TSShowMetaItemCustomPropertiesAction(mySettings))
        add(TSGroupItemByParentAction(mySettings))
        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 74100584202830949L

        const val ID = "Type System"
    }

}