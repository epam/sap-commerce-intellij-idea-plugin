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

package sap.commerce.toolset.project

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

sealed class ProjectState {
    data object Normal : ProjectState()
    data object ForceReimport : ProjectState()

    data class Reimport(
        val importedByVersion: String,
        val reimportRequests: List<PullRequest> = emptyList(),
        val refreshRequests: List<PullRequest> = emptyList(),
    ) : ProjectState()

    data class Refresh(
        val importedByVersion: String,
        val refreshRequests: List<PullRequest>,
    ) : ProjectState()

    data class PullRequest(
        val title: String,
        val number: Int,
        val author: String,
        val milestone: String,
        val labels: List<String>
    ) {
        val type: RequestType
            get() = RequestType.fromLabel(labels)
    }

    data class PRData(
        val pullRequests: List<PullRequest>
    )

    // Order or enum values is important
    enum class RequestType(val label: String? = null, val presentationTitle: String, val icon: Icon) {
        REIMPORT(
            label = ProjectImportConstants.PR_LABEL_PROJECT_REIMPORT,
            presentationTitle = "Reimport request",
            icon = HybrisIcons.Project.REIMPORT
        ),
        REFRESH(
            label = ProjectImportConstants.PR_LABEL_PROJECT_REFRESH,
            presentationTitle = "Refresh request",
            icon = HybrisIcons.Project.REFRESH
        ),
        OTHER(
            presentationTitle = "Other",
            icon = HybrisIcons.Project.UNKNOWN_REQUEST
        );

        companion object {
            fun fromLabel(labels: List<String>): RequestType = entries
                .find { labels.contains(it.label) }
                ?: OTHER
        }
    }
}
