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

package com.intellij.idea.plugin.hybris.tools.ccv2.dto

data class CCv2Build(
    val code: String,
    val name: String,
    val branch: String,
    val status: CCv2BuildStatus,
    val appCode: String,
    val appDefVersion: String,
    val createdBy: String,
    val startTime: String,
    val endTime: String,
    val buildVersion: String,
    val version: String,
) : CCv2DTO

enum class CCv2BuildStatus(val title: String) {
    UNKNOWN("Unknown"),
    SCHEDULED("Scheduled"),
    BUILDING("Building"),
    SUCCESS("Success"),
    FAIL("Fail"),
    DELETED("Deleted");

    companion object {
        fun tryValueOf(name: String) = CCv2BuildStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}