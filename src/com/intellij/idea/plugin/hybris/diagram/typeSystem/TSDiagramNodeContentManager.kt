/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.AbstractDiagramNodeContentManager
import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramCategory
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.*

class TSDiagramNodeContentManager : AbstractDiagramNodeContentManager() {

    override fun getContentCategories() = CATEGORIES

    override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?) = when (item) {
        is TSGraphFieldProperty -> category == PROPERTIES
        is TSGraphFieldTyped -> category == PROPERTIES
        is TSGraphFieldDeployment -> category == DEPLOYMENT
        is TSGraphFieldAttribute -> category == ATTRIBUTES
        is TSGraphFieldRelationEnd -> category == RELATION_ENDS
        is TSGraphFieldRelationElement -> category == RELATION_ENDS
        is TSGraphFieldCustomProperty -> category == CUSTOM_PROPERTIES
        is TSGraphFieldIndex -> category == INDEXES
        is TSGraphFieldEnumValue -> category == ENUM_VALUES
        else -> false
    }

    companion object {
        val PROPERTIES = DiagramCategory({ message("hybris.diagram.ts.provider.category.properties") }, HybrisIcons.TypeSystem.Diagram.PROPERTY, true, false)
        val DEPLOYMENT = DiagramCategory({ message("hybris.diagram.ts.provider.category.deployment") }, HybrisIcons.TypeSystem.Diagram.DEPLOYMENT, true, false)
        val ATTRIBUTES = DiagramCategory({ message("hybris.diagram.ts.provider.category.attributes") }, HybrisIcons.TypeSystem.ATTRIBUTE, true, false)
        val RELATION_ENDS = DiagramCategory({ message("hybris.diagram.ts.provider.category.relation_ends") }, HybrisIcons.TypeSystem.RELATION, true, false)
        val CUSTOM_PROPERTIES = DiagramCategory({ message("hybris.diagram.ts.provider.category.custom_properties") }, HybrisIcons.TypeSystem.CUSTOM_PROPERTY, true, false)
        val INDEXES = DiagramCategory({ message("hybris.diagram.ts.provider.category.indexes") }, HybrisIcons.TypeSystem.INDEX, true, false)
        val ENUM_VALUES = DiagramCategory({ message("hybris.diagram.ts.provider.category.enum_values") }, HybrisIcons.TypeSystem.ENUM_VALUE, true, false)
        val CATEGORIES = arrayOf(PROPERTIES, CUSTOM_PROPERTIES, ATTRIBUTES, RELATION_ENDS, INDEXES, ENUM_VALUES, DEPLOYMENT)
    }

}
