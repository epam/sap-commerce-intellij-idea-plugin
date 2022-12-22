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

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ShowOnlyCustomAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.action.only_custom.text"), message("hybris.toolwindow.ts.action.only_custom.description"), null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowOnlyCustom()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowOnlyCustom(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaItemsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.items.text"), null, AllIcons.Actions.GroupByClass) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaItems()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaItems(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaMapsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.maps.text"), null, AllIcons.Actions.GroupByPackage) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaMaps()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaMaps(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaEnumsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.enums.text"), null, AllIcons.Actions.GroupByTestProduction) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaEnums()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaEnums(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaCollectionsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.collections.text"), null, AllIcons.Actions.GroupByPrefix) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaCollections()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaCollections(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaRelationsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.relations.text"), null, AllIcons.Actions.GroupByModuleGroup) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaRelations()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaRelations(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaAtomicsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.atomics.text"), null, AllIcons.Actions.GroupByModule) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaAtomics()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaAtomics(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
