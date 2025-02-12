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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui

import com.intellij.ide.HelpTooltip
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Util
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.notification.NotificationType
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import java.awt.datatransfer.StringSelection
import java.time.OffsetDateTime
import javax.swing.Icon

fun Row.date(label: String, dateTime: OffsetDateTime?) {
    label(CCv2Util.formatTime(dateTime))
        .comment(label)
}

fun Row.copyLink(project: Project, label: String?, value: String, confirmationMessage: String = "Copied to clipboard") {
    link(value) {
        CopyPasteManager.getInstance().setContents(StringSelection(value))
        Notifications.create(NotificationType.INFORMATION, confirmationMessage, "")
            .hideAfter(10)
            .notify(project)
    }
        .comment(label)
        .applyToComponent {
            HelpTooltip()
                .setTitle("Click to copy to clipboard")
                .installOn(this);
        }
}

fun Row.sUser(project: Project, sUserId: String, icon: Icon, label: String = "Created by") {
    icon(icon)
        .gap(RightGap.SMALL)
    val sUser = DeveloperSettingsComponent.getInstance(project).getSUser(sUserId)
    link(sUser.toString()) { SUserDetailsDialog(project, sUser).showAndGet() }
        .comment(label)
        .applyToComponent {
            HelpTooltip()
                .setTitle("Define an alias for the S-User")
                .installOn(this);
        }
}

fun Row.dynatrace(environment: CCv2EnvironmentDto) {
    icon(HybrisIcons.CCv2.DYNATRACE)
        .gap(RightGap.SMALL)
    browserLink("Dynatrace", environment.dynatraceLink ?: "")
        .enabled(environment.dynatraceLink != null)
        .comment(
            environment.problems
            ?.let { "problems: <strong>$it</strong>" } ?: "&nbsp;")
}

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
                    foreground = JBColor.namedColor("hybris.ccv2.service.running", 0x59A869, 0x499C54)
                }
            }

            service.availableReplicas != service.desiredReplicas -> label("Deploying")
            else -> label("--")
        }

        statusLabel
            .comment("Status")
    }
}

fun Panel.ccv2StatusYesNo(status: Boolean, comment: String) {
    row {
        val statusLabel = when (status) {
            false -> label("No").also {
                with(it.component) {
                    foreground = JBColor.namedColor("hybris.ccv2.status.no", 0xDB5860, 0xC75450)
                }
            }

            true -> label("Yes").also {
                with(it.component) {
                    foreground = JBColor.namedColor("hybris.ccv2.status.yes", 0x59A869, 0x499C54)
                }
            }
        }

        statusLabel
            .comment(comment)
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
        icon(HybrisIcons.CCv2.Service.MODIFIED_BY)
            .gap(RightGap.SMALL)
        label(service.modifiedBy)
            .comment("Modified by")
    }
}

fun Panel.ccv2ServiceModifiedTimeRow(service: CCv2ServiceDto) {
    row {
        date("Modified time", service.modifiedTime)
    }
}