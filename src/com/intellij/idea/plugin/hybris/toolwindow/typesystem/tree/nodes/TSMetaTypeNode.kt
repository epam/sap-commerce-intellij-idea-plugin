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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.TSViewSettings
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import java.util.*

class TSMetaTypeNode(parent: TSNode, private val metaType: TSMetaType) : TSNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = HybrisI18NBundleUtils.message("hybris.toolwindow.ts.group.${metaType.name.lowercase()}.name")

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            TSMetaType.META_ATOMIC -> presentation.setIcon(AllIcons.Actions.GroupByModule)
            TSMetaType.META_ITEM -> presentation.setIcon(AllIcons.Actions.GroupByClass)
            TSMetaType.META_ENUM -> presentation.setIcon(AllIcons.Actions.GroupByTestProduction)
            TSMetaType.META_COLLECTION -> presentation.setIcon(AllIcons.Actions.GroupByPrefix)
            TSMetaType.META_MAP -> presentation.setIcon(AllIcons.Actions.GroupByPackage)
            TSMetaType.META_RELATION -> presentation.setIcon(AllIcons.Actions.GroupByModuleGroup)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)


        val showOnlyCustom = TSViewSettings.getInstance(myProject).isShowOnlyCustom()
        val entries = TSMetaModelAccess.getInstance(myProject).getMetaModel().getMetaType<TSGlobalMetaClassifier<DomElement>>(metaType).values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .size
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getChildren(): Collection<TSNode?> {
        val showOnlyCustom = TSViewSettings.getInstance(myProject).isShowOnlyCustom()

        return TSMetaModelAccess.getInstance(myProject).getMetaModel()
            .getMetaType<TSGlobalMetaClassifier<DomElement>>(metaType).values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map {
                when (it) {
                    is TSGlobalMetaItem -> TSMetaItemNode(this, it)
                    is TSGlobalMetaEnum -> TSMetaEnumNode(this, it)
                    is TSGlobalMetaRelation -> TSMetaRelationNode(this, it)
                    is TSGlobalMetaCollection -> TSMetaCollectionNode(this, it)
                    is TSGlobalMetaAtomic -> TSMetaAtomicNode(this, it)
                    is TSGlobalMetaMap -> TSMetaMapNode(this, it)
                    else -> null
                }
            }
            .filter { Objects.nonNull(it) }
            .sortedBy { it!!.name }
    }

}