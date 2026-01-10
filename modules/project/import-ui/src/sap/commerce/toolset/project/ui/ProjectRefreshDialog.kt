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

import com.intellij.icons.AllIcons
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isSandbox
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.ui.banner
import java.awt.Dimension
import javax.swing.Icon
import javax.swing.ScrollPaneConstants

class ProjectRefreshDialog(
    project: Project,
    private val refreshContext: ProjectRefreshContext.Mutable,
) : DialogWrapper(project) {

    private var ui = panel {
        group("Cleanup") {
            row {
                checkBox("Re-create [y] modules")
                    .comment(
                        """
                        Experimental feature! Modules with be removed and recreated from the scratch.<br>
                        Hopefully, will be released at some point. There is a need to properly support complex modificiations of the modules configurations.<br>
                        As for now, this feature is not available for production environments.
                    """.trimIndent()
                    )
                    .enabled(isSandbox)
                    .bindSelected(refreshContext.removeOldProjectData)
                icon(AllIcons.General.Warning)
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
                contextHelp(i18n("hybris.project.import.followSymlink.help.description"))
            }

            row {
                checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                    .bindSelected(refreshContext.importSettings.importOOTBModulesInReadOnlyMode)
                contextHelp(i18n("hybris.import.wizard.import.ootb.modules.read.only.help.description"))
            }

            row {
                checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                    .bindSelected(refreshContext.importSettings.useFakeOutputPathForCustomExtensions)
                contextHelp(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.help.description"))
            }

            row {
                checkBox(i18n("hybris.project.import.ignoreNonExistingSourceDirectories"))
                    .bindSelected(refreshContext.importSettings.ignoreNonExistingSourceDirectories)
            }

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(refreshContext.importSettings.importCustomAntBuildFiles)
                contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.help.description"))
            }

            row {
                checkBox(i18n("hybris.project.import.withStandardProvidedSources"))
                    .bindSelected(refreshContext.importSettings.withStandardProvidedSources)
                contextHelp(i18n("hybris.project.import.withStandardProvidedSources.help.description"))
            }

            row {
                label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                    .bindSelected(refreshContext.importSettings.withExternalLibrarySources)

                checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                    .bindSelected(refreshContext.importSettings.withExternalLibraryJavadocs)

                contextHelp(i18n("hybris.project.import.withExternalLibrarySources.help.description"))
            }
        }

        collapsibleGroup(i18n("hybris.project.import.projectStructure.title")) {
            row {
                checkBox(i18n("hybris.project.view.tree.hide.empty.middle.folders"))
                    .bindSelected(refreshContext.importSettings.hideEmptyMiddleFolders)
            }

            row {
                checkBox(i18n("hybris.import.settings.group.modules"))
                    .bindSelected(refreshContext.importSettings.groupModules)
            }

            indent {
                row {
                    comment(i18n("hybris.import.settings.group.modules.help"))
                }

                groupProperties(
                    HybrisIcons.Extension.CUSTOM,
                    i18n("hybris.import.settings.group.custom"),
                    i18n("hybris.import.settings.group.unused"),
                    refreshContext.importSettings.groupCustom,
                    refreshContext.importSettings.groupOtherCustom,
                    refreshContext.importSettings.groupModules
                )
                groupProperties(
                    HybrisIcons.Extension.OOTB,
                    i18n("hybris.import.settings.group.hybris"),
                    i18n("hybris.import.settings.group.unused"),
                    refreshContext.importSettings.groupHybris,
                    refreshContext.importSettings.groupOtherHybris,
                    refreshContext.importSettings.groupModules
                )
                groupProperties(
                    HybrisIcons.Extension.PLATFORM,
                    i18n("hybris.import.settings.group.platform"),
                    i18n("hybris.import.settings.group.nonhybris"),
                    refreshContext.importSettings.groupPlatform,
                    refreshContext.importSettings.groupNonHybris,
                    refreshContext.importSettings.groupModules
                )

                row {
                    icon(HybrisIcons.Module.CCV2_GROUP)
                    groupProperty(
                        i18n("hybris.import.settings.group.ccv2"),
                        refreshContext.importSettings.groupCCv2,
                        refreshContext.importSettings.groupModules
                    )
                }.layout(RowLayout.PARENT_GRID)
            }.visibleIf(refreshContext.importSettings.groupModules)

            row {
                checkBox("Group external modules")
                    .bindSelected(refreshContext.importSettings.groupExternalModules)
                contextHelp(i18n("hybris.project.view.external.module.help.description"))
            }

            indent {
                row {
                    icon(HybrisIcons.Module.EXTERNAL_GROUP)
                    groupProperty(
                        i18n("hybris.import.settings.group.externalModules"),
                        refreshContext.importSettings.groupNameExternalModules,
                        refreshContext.importSettings.groupExternalModules
                    )
                }
            }.visibleIf(refreshContext.importSettings.groupExternalModules)
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16, 32, 16)
        registerValidators(disposable)
    }

    init {
        title = "Refresh the Project"
        super.init()
    }

    override fun createCenterPanel() = panel {
        row {
            scrollCell(ui)
                .align(Align.FILL)
                .applyToComponent {
                    parent.parent.asSafely<JBScrollPane>()
                        ?.apply {
                            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                            border = JBEmptyBorder(0)
                        }
                }

        }.resizableRow()
    }

    override fun createNorthPanel() = banner(
        text = "Other settings can be found under SAP CX Settings.",
    )

    override fun getPreferredSize() = Dimension(JBUI.DialogSizes.medium().width, JBUIScale.scale(500))
    override fun getStyle(): DialogStyle = DialogStyle.COMPACT
    override fun doValidateAll() = ui.validateAll()
        .filter { it.component?.isVisible ?: false }
        .onEach { it.okEnabled = true }

    override fun applyFields() {
        ui.apply()
    }

    private fun Panel.groupProperties(
        icon: Icon,
        groupLabel: String,
        groupOtherLabel: String,
        groupProperty: ObservableMutableProperty<String>,
        groupOtherProperty: ObservableMutableProperty<String>,
        enabledIf: ObservableProperty<Boolean>
    ) {
        row {
            icon(icon)
            groupProperty(groupLabel, groupProperty, enabledIf)
            groupProperty(groupOtherLabel, groupOtherProperty, enabledIf)
        }.layout(RowLayout.PARENT_GRID)
    }

    private fun Row.groupProperty(
        groupLabel: String,
        groupProperty: ObservableMutableProperty<String>,
        enabledIf: ObservableProperty<Boolean>
    ) {
        label(groupLabel)
        textField()
            .bindText(groupProperty)
            .addValidationRule(i18n("hybris.settings.validations.notBlank")) { it.text.isBlank() }
            .enabledIf(enabledIf)
            .applyIfEnabled()
    }
}