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

package com.intellij.idea.plugin.hybris.tools.ccm

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DTO
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

abstract class AbstractSAPCCMListCommand<T : CCv2DTO>(
    protected val name: String,
    protected val command: String,
    private val headers: List<String>,
) {
    // TODO: use Kotlin coroutines for parallel processing
    fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>,
        transform: (String, Map<String, Int>) -> T,
    ) = subscriptions.associateWith { subscription ->
        val parameters = arrayOf(command, "list", "--subscription-code=${subscription.id}")

        ProgressManager.getInstance().progressIndicator.text2 = "Fetching $name for subscription: $subscription"

        SAPCCM.execute(project, appSettings, *parameters)
            ?.let { SAPCCM.transformResult(headers, it, transform) }
            ?: emptyList()
    }
}

