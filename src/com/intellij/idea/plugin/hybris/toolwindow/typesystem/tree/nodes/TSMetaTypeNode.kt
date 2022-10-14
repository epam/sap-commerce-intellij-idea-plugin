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
import com.intellij.idea.plugin.hybris.type.system.meta.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import java.util.*

class TSMetaTypeNode(parent: TSNode, private val metaType: MetaType) : TSNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = HybrisI18NBundleUtils.message("hybris.toolwindow.ts.group.${metaType.name.lowercase()}.name")

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            MetaType.META_ATOMIC -> presentation.setIcon(AllIcons.Actions.GroupByModule)
            MetaType.META_ITEM -> presentation.setIcon(AllIcons.Actions.GroupByClass)
            MetaType.META_ENUM -> presentation.setIcon(AllIcons.Actions.GroupByTestProduction)
            MetaType.META_COLLECTION -> presentation.setIcon(AllIcons.Actions.GroupByPrefix)
            MetaType.META_MAP -> presentation.setIcon(AllIcons.Actions.GroupByPackage)
            MetaType.META_RELATION -> presentation.setIcon(AllIcons.Actions.GroupByModuleGroup)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val entries = TSMetaModelAccess.getInstance(myProject).metaModel.getMetaType<TSMetaClassifier<DomElement>>(metaType).values.size
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getChildren(): Collection<TSNode?> = TSMetaModelAccess.getInstance(myProject).metaModel
            .getMetaType<TSMetaClassifier<DomElement>>(metaType).values
            .map {
                when (it) {
                    is TSMetaItem -> TSMetaItemNode(this, it)
                    is TSMetaEnum -> TSMetaEnumNode(this, it)
                    is TSMetaRelation -> TSMetaRelationNode(this, it)
                    is TSMetaCollection -> TSMetaCollectionNode(this, it)
                    is TSMetaAtomic -> TSMetaAtomicNode(this, it)
                    is TSMetaMap -> TSMetaMapNode(this, it)
                    else -> null
                }
            }
            .filter { Objects.nonNull(it) }
            .sortedBy { it!!.name }

}