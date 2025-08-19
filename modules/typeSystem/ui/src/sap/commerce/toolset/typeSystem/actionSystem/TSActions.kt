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

package sap.commerce.toolset.typeSystem.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.settings.TSViewSettings
import sap.commerce.toolset.typeSystem.settings.state.ChangeType

class TSShowOnlyCustomAction(val settings: TSViewSettings) :
    ToggleAction(i18n("hybris.toolwindow.action.only_custom.text"), i18n("hybris.toolwindow.ts.action.only_custom.description"), null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showOnlyCustom

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showOnlyCustom = state
        settings.fireSettingsChanged(ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaItemsAction(val settings: TSViewSettings) :
    ToggleAction(i18n("hybris.toolwindow.ts.action.items.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_ITEMS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaItems

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaItems = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaMapsAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.maps.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_MAPS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaMaps

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaMaps = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaEnumsAction(val settings: TSViewSettings) :
    ToggleAction(i18n("hybris.toolwindow.ts.action.enums.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_ENUMS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaEnums

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaEnums = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaCollectionsAction(val settings: TSViewSettings) :
    ToggleAction(i18n("hybris.toolwindow.ts.action.collections.text"), null, HybrisIcons.TypeSystem.Preview.Actions.SHOW_COLLECTIONS) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaCollections

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaCollections = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaRelationsAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.relations.text"), null, HybrisIcons.TypeSystem.Types.RELATION) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaRelations

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaRelations = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaAtomicsAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.atomics.text"), null, HybrisIcons.TypeSystem.Types.ATOMIC) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaAtomics

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaAtomics = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaEnumValuesAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.enum.values.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaEnumValues

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaEnumValues = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaItemIndexesAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.item.indexes.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaItemIndexes

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaItemIndexes = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaItemAttributesAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.item.attributes.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaItemAttributes

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaItemAttributes = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSShowMetaItemCustomPropertiesAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.item.custom_properties.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.showMetaItemCustomProperties

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.showMetaItemCustomProperties = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class TSGroupItemByParentAction(val settings: TSViewSettings) : ToggleAction(i18n("hybris.toolwindow.ts.action.item.group_by_parent.text"), null, null) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.groupItemByParent

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.groupItemByParent = state
        settings.fireSettingsChanged(ChangeType.UPDATE)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
