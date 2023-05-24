/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.common.services;

import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

interface CommonIdeaService {

    fun isTypingActionInProgress(): Boolean

    fun isPotentiallyHybrisProject(project: Project): Boolean

    fun getPlatformDescriptor(hybrisProjectDescriptor: HybrisProjectDescriptor): PlatformHybrisModuleDescriptor?

    fun getActiveHacUrl(project: Project): String

    fun getHostHacUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String

    fun getSolrUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String

    fun fixRemoteConnectionSettings(project: Project)

    fun getActiveSslProtocol(project: Project, settings: HybrisRemoteConnectionSettings?): String

    companion object {
        val instance: CommonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService::class.java)
    }
}
