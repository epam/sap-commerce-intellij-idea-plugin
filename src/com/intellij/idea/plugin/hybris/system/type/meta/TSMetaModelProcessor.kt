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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.system.meta.MetaModelProcessor
import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelBuilder
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class TSMetaModelProcessor(project: Project) : MetaModelProcessor<Items, TSMetaModel>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: Items): TSMetaModel =
        with(TSMetaModelBuilder(container, yContainer, fileName, custom)) {
            withItemTypes(dom.itemTypes.itemTypes)
            withItemTypes(dom.itemTypes.typeGroups.flatMap { it.itemTypes })
            withEnumTypes(dom.enumTypes.enumTypes)
            withAtomicTypes(dom.atomicTypes.atomicTypes)
            withCollectionTypes(dom.collectionTypes.collectionTypes)
            withRelationTypes(dom.relations.relations)
            withMapTypes(dom.mapTypes.mapTypes)
            build()
        }
}