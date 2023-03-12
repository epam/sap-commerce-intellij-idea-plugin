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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node

import com.intellij.diagram.DiagramRelationshipInfoAdapter
import com.intellij.diagram.presentation.DiagramLineType
import java.awt.Shape

class TSDiagramRelationship(
    name: String? = null,
    upperCenterLabel: String? = null,
    bottomCenterLabel: String? = null,
    upperSourceLabel: String? = null,
    bottomSourceLabel: String? = null,
    upperTargetLabel: String? = null,
    bottomTargetLabel: String? = null,
    lineType: DiagramLineType = DiagramLineType.SOLID,
    width: Int = 1,
    sourceArrow: Shape? = null,
    targetArrow: Shape? = null,
) : DiagramRelationshipInfoAdapter(name, lineType, width, sourceArrow, targetArrow, upperCenterLabel, bottomCenterLabel, upperSourceLabel, bottomSourceLabel, upperTargetLabel, bottomTargetLabel)