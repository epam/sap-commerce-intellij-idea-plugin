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

package sap.commerce.toolset.typeSystem.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.typeSystem.settings.TSViewSettings

class ShowOnlyCustomAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.action.only_custom.text"), message("hybris.toolwindow.ts.action.only_custom.description"), null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowOnlyCustom()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowOnlyCustom(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaItemsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.items.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_ITEMS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaItems()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaItems(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaMapsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.maps.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_MAPS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaMaps()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaMaps(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaEnumsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.enums.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_ENUMS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaEnums()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaEnums(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaCollectionsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.collections.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_COLLECTIONS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaCollections()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaCollections(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaRelationsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.relations.text"), null, HybrisIcons.TypeSystem.Types.RELATION) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaRelations()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaRelations(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaAtomicsAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.atomics.text"), null, HybrisIcons.TypeSystem.Types.ATOMIC) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaAtomics()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaAtomics(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaEnumValuesAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.enum.values.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaEnumValues()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaEnumValues(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaItemIndexesAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.item.indexes.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaItemIndexes()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaItemIndexes(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaItemAttributesAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.item.attributes.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaItemAttributes()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaItemAttributes(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaItemCustomPropertiesAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.item.custom_properties.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowMetaItemCustomProperties()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowMetaItemCustomProperties(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class GroupItemByParentAction(val settings: TSViewSettings) : ToggleAction(message("hybris.toolwindow.ts.action.item.group_by_parent.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isGroupItemByParent()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setGroupItemByParent(state)
        settings.fireSettingsChanged(TSViewSettings.ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
