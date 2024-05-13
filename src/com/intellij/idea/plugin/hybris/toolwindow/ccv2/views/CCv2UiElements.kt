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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2.views

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap

fun Panel.ccv2ServiceStatusRow(service: CCv2ServiceDto) {
    row {
        val statusLabel = when {
            service.desiredReplicas == null -> label("--")
            service.availableReplicas == 0 -> label("Stopped").also {
                with(it.component) {
                    foreground = JBColor.namedColor("hybris.ccv2.service.stopped", 0xDB5860, 0xC75450)
                }
            }

            service.availableReplicas == service.desiredReplicas -> label("Running").also {
                with(it.component) {
                    foreground = JBColor.namedColor("hybris.ccv2.service.stopped", 0x59A869, 0x499C54)
                }
            }

            service.availableReplicas != service.desiredReplicas -> label("Deploying")
            else -> label("--")
        }

        statusLabel
            .comment("Status")
    }
}

fun Panel.ccv2ServiceReplicasRow(service: CCv2ServiceDto) {
    row {
        val replicas = if (service.desiredReplicas != null && service.availableReplicas != null)
            "${service.availableReplicas} / ${service.desiredReplicas}"
        else "--"
        label(replicas)
            .comment("Replicas")
    }
}

fun Panel.ccv2ServiceModifiedByRow(service: CCv2ServiceDto) {
    row {
        icon(HybrisIcons.CCV2_SERVICE_MODIFIED_BY)
            .gap(RightGap.SMALL)
        label(service.modifiedBy)
            .comment("Modified by")
    }
}