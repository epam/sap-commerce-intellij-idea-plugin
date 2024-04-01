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
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.ProgressReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

abstract class AbstractSAPCCMListCommand<T : CCv2DTO>(
    protected val name: String,
    protected val command: String,
    private val headers: List<String>,
) {

    suspend fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        progressReporter: ProgressReporter,
        subscriptions: Collection<CCv2Subscription>,
        transform: (String, Map<String, Int>) -> T,
    ): SortedMap<CCv2Subscription, Collection<T>> {
        val result = sortedMapOf<CCv2Subscription, Collection<T>>()

        coroutineScope {
            subscriptions.forEach {
                launch {
                    result[it] = progressReporter.sizedStep(1, "Fetching $name for subscription: $it") {
                        list(project, appSettings, it, transform)
                    }
                }
            }
        }

        return result
    }

    suspend fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscription: CCv2Subscription,
        transform: (String, Map<String, Int>) -> T
    ): List<T> = withContext(Dispatchers.IO) {
        val parameters = arrayOf(command, "list", "--subscription-code=${subscription.id}")

        SAPCCM.execute(project, appSettings, *parameters)
            ?.let { SAPCCM.transformResult(headers, it, transform) }
            ?: emptyList()
    }
}

