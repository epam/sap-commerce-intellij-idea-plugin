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

package sap.commerce.toolset.ccv2

import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.groovy.exec.context.ReplicaSelectionMode
import sap.commerce.toolset.i18n

object CCv2ExecConstants {

    val ccv2 by lazy {
        ReplicaSelectionMode(
            title = "CCv2",
            description = "Select id of the CCv2 service specific replica",
            icon = HybrisIcons.CCv2.DESCRIPTOR,
            presentationText = i18n("hybris.groovy.actions.executionMode.ccv2"),
            presentationDescription = i18n("hybris.groovy.actions.executionMode.ccv2.description"),
            previewText = { _ -> "Auto-Discover Replica" },
            previewDescription = { context -> "- CCv2 ${context.replicaContexts.size} replica(s) -" }
        )
    }

}