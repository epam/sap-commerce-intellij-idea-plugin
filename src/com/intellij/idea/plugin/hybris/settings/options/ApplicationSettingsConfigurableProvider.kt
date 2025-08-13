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
package com.intellij.idea.plugin.hybris.settings.options

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class ApplicationSettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "[y] SAP Commerce", "[y] SAP CX configuration."
    ) {

        private val applicationSettings = ApplicationSettings.getInstance()

        override fun createPanel() = panel {
            row {
                checkBox(message("hybris.import.settings.import.ootb.modules.read.only.label"))
                    .comment(message("hybris.import.settings.import.ootb.modules.read.only.tooltip"))
                    .bindSelected(applicationSettings::defaultPlatformInReadOnly)
            }
            row {
                checkBox(message("hybris.project.import.scanExternalModules"))
                    .bindSelected(applicationSettings::scanThroughExternalModule)
            }
            row {
                checkBox(message("hybris.project.import.followSymlink"))
                    .bindSelected(applicationSettings::followSymlink)
            }
            row {
                checkBox(message("hybris.project.view.tree.hide.empty.middle.folders"))
                    .bindSelected(applicationSettings::hideEmptyMiddleFolders)
            }
            row {
                checkBox(message("hybris.project.import.ignore.non.existing.sources"))
                    .bindSelected(applicationSettings::ignoreNonExistingSourceDirectories)
            }
            row {
                checkBox(message("hybris.project.attach.standard.sources"))
                    .bindSelected(applicationSettings::withStandardProvidedSources)
            }
            row {
                checkBox(message("hybris.project.import.excludeTestSources"))
                    .bindSelected(applicationSettings::excludeTestSources)
            }
            row {
                checkBox(message("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(applicationSettings::importCustomAntBuildFiles)
            }
            row {
                checkBox(message("hybris.ts.items.validation.settings.enabled"))
                    .bindSelected(applicationSettings::warnIfGeneratedItemsAreOutOfDate)
            }
        }
    }
}
