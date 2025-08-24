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

package sap.commerce.toolset.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.settings.ApplicationSettings

class ApplicationSettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "[y] SAP Commerce", "[y] SAP CX configuration."
    ) {

        private val applicationSettings = ApplicationSettings.getInstance()

        override fun createPanel() = panel {
            row {
                checkBox(i18n("hybris.import.settings.import.ootb.modules.read.only.label"))
                    .comment(i18n("hybris.import.settings.import.ootb.modules.read.only.tooltip"))
                    .bindSelected(applicationSettings::defaultPlatformInReadOnly)
            }
            row {
                checkBox(i18n("hybris.project.import.scanExternalModules"))
                    .bindSelected(applicationSettings::scanThroughExternalModule)
            }
            row {
                checkBox(i18n("hybris.project.import.followSymlink"))
                    .bindSelected(applicationSettings::followSymlink)
            }
            row {
                checkBox(i18n("hybris.project.view.tree.hide.empty.middle.folders"))
                    .bindSelected(applicationSettings::hideEmptyMiddleFolders)
            }
            row {
                checkBox(i18n("hybris.project.import.ignore.non.existing.sources"))
                    .bindSelected(applicationSettings::ignoreNonExistingSourceDirectories)
            }
            row {
                checkBox(i18n("hybris.project.attach.standard.sources"))
                    .bindSelected(applicationSettings::withStandardProvidedSources)
            }
            row {
                checkBox(i18n("hybris.project.import.excludeTestSources"))
                    .bindSelected(applicationSettings::excludeTestSources)
            }
            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(applicationSettings::importCustomAntBuildFiles)
            }
            row {
                checkBox(i18n("hybris.ts.items.validation.settings.enabled"))
                    .bindSelected(applicationSettings::warnIfGeneratedItemsAreOutOfDate)
            }
        }
    }
}