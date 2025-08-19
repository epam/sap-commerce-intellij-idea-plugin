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

package sap.commerce.toolset.groovy

import com.intellij.icons.AllIcons
import sap.commerce.toolset.groovy.exec.context.ReplicaSelectionMode
import sap.commerce.toolset.i18n

object GroovyExecConstants {
    val auto by lazy {
        ReplicaSelectionMode(
            title = "Auto-discover",
            description = "Automatically discover replica",
            icon = AllIcons.Actions.Lightning,
            previewText = { _ -> "Auto-discover replica" },
            presentationText = i18n("hybris.groovy.actions.executionMode.auto"),
            presentationDescription = i18n("hybris.groovy.actions.executionMode.auto.description"),
        )
    }
    val manual by lazy {
        ReplicaSelectionMode(
            title = "Manual",
            description = "Manually specify replica id and corresponding cookie name",
            icon = AllIcons.Actions.Edit,
            presentationText = i18n("hybris.groovy.actions.executionMode.manual"),
            presentationDescription = i18n("hybris.groovy.actions.executionMode.manual.description"),
            previewDescription = { context ->
                listOfNotNull(
                    "- Manually configured replica(s) -",
                    context.replicaContexts.groupBy { it.cookieName }
                        .map { (cookieName, ids) ->
                            "Cookie: $cookieName (${ids.size} replica(s))"
                        }
                ).joinToString("\n")
            }
        )
    }
}