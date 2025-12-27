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

package sap.commerce.toolset.project.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.ui.banner

class ProjectRefreshDialog(
    project: Project,
    private val refreshContext: ProjectRefreshContext.Mutable,
) : DialogWrapper(project) {

    init {
        title = "Refresh the Project"
        super.init()
    }

    override fun getStyle(): DialogStyle = DialogStyle.COMPACT

    override fun createNorthPanel() = banner(
        text = "Other settings can be found under SAP CX Settings.",
    )

    override fun createCenterPanel() = panel {
        group("Cleanup") {
            row {
                checkBox("Remove old project data")
                    .comment("Experimental feature! Modules with respective .iml files will not be removed on refresh.")
                    .bindSelected(refreshContext.removeOldProjectData)
            }

            row {
                checkBox("Remove external modules")
                    .bindSelected(refreshContext.removeExternalModules)
                contextHelp("Non SAP Commerce external modules will be removed during the project refresh.")
            }
        }

        group(i18n("hybris.project.import.projectImportSettings.title")) {
            row {
                checkBox(i18n("hybris.project.import.followSymlink"))
                    .bindSelected(refreshContext.importSettings.followSymlink)
            }

            row {
                checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                    .bindSelected(refreshContext.importSettings.importOOTBModulesInReadOnlyMode)
                contextHelp(i18n("hybris.import.wizard.import.ootb.modules.read.only.tooltip"))
            }

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(refreshContext.importSettings.importCustomAntBuildFiles)
                contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.tooltip"))
            }

            row {
                checkBox(i18n("hybris.project.import.withStandardProvidedSources"))
                    .bindSelected(refreshContext.importSettings.withStandardProvidedSources)
                contextHelp(i18n("hybris.project.import.withStandardProvidedSources.tooltip"))
            }

            row {
                label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                    .bindSelected(refreshContext.importSettings.withExternalLibrarySources)

                checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                    .bindSelected(refreshContext.importSettings.withExternalLibraryJavadocs)

                contextHelp(i18n("hybris.project.import.withExternalLibrarySources.tooltip"))
            }
        }

        group("Project Structure") {
            row {
                checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                    .bindSelected(refreshContext.importSettings.useFakeOutputPathForCustomExtensions)
                contextHelp(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.tooltip"))
            }

            row {
                checkBox(i18n("hybris.project.import.excludeTestSources"))
                    .bindSelected(refreshContext.importSettings.excludeTestSources)
            }

            row {
                checkBox(i18n("hybris.project.import.ignoreNonExistingSourceDirectories"))
                    .bindSelected(refreshContext.importSettings.ignoreNonExistingSourceDirectories)
            }
        }
    }.apply {
        this.border = JBUI.Borders.empty(8, 16)
    }
}