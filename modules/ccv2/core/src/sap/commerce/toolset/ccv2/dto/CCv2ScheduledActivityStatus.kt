/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.ccv2.dto

import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.model.ScheduledActivityDetailDTO
import javax.swing.Icon

enum class CCv2ScheduledActivityStatus(val title: String, val icon: Icon = HybrisIcons.CCv2.SCHEDULED_ACTIVITIES) {
    SCHEDULED("Scheduled"),
    QUEUED("Queued"),
    QUEUED_AWAITING_LOCK("Queued (awaiting lock)"),
    RUNNING("Running"),
    SUCCESS("Success"),
    ERROR("Error"),
    CANCELING("Canceling"),
    CANCELED("Canceled");

    companion object {
        fun of(status: ScheduledActivityDetailDTO.Status) = when (status) {
            ScheduledActivityDetailDTO.Status.SCHEDULED -> SCHEDULED
            ScheduledActivityDetailDTO.Status.QUEUED -> QUEUED
            ScheduledActivityDetailDTO.Status.QUEUED_AWAITING_LOCK -> QUEUED_AWAITING_LOCK
            ScheduledActivityDetailDTO.Status.RUNNING -> RUNNING
            ScheduledActivityDetailDTO.Status.SUCCESS -> SUCCESS
            ScheduledActivityDetailDTO.Status.ERROR -> ERROR
            ScheduledActivityDetailDTO.Status.CANCELING -> CANCELING
            ScheduledActivityDetailDTO.Status.CANCELED -> CANCELED
        }
    }
}