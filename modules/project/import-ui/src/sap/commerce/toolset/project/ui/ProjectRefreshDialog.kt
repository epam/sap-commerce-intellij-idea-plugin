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

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import com.intellij.util.text.VersionComparatorUtil
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.settings.WorkspaceSettings
import sap.commerce.toolset.ui.banner
import javax.swing.Icon
import javax.swing.ScrollPaneConstants

class ProjectRefreshDialog(
    private val project: Project,
    private val refreshContext: ProjectRefreshContext.Mutable,
) : DialogWrapper(project) {

    private val canRefresh by lazy {
        val importedByVersion = WorkspaceSettings.getInstance(project).importedByVersion
            ?: return@lazy false

        return@lazy VersionComparatorUtil.compare(ProjectImportConstants.MIN_IMPORT_API_VERSION, importedByVersion) <= 0
    }

    private var ui = panel {
        group("Cleanup") {
            row {
                checkBox("Remove external modules")
                    .bindSelected(refreshContext.removeExternalModules)
                contextHelp("Non SAP Commerce external modules will be removed during the project refresh.")
            }
        }

        group(i18n("hybris.project.import.projectImportSettings.title")) {
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
                contextHelp(i18n("hybris.project.import.ignoreNonExistingSourceDirectories.help.description"))
            }

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(refreshContext.importSettings.importCustomAntBuildFiles)
                contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.help.description"))
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
        isOKActionEnabled = canRefresh
    }

    override fun createCenterPanel() = panel {
        if (canRefresh) refreshScrollableUi()
        else row {
            cell(onlyImportUi())
        }
    }

    private fun onlyImportUi() = panel {
        row {
            text(
                """
This project cannot be refreshed using the current version of the plugin.
<br>
The plugin’s project import and refresh implementation has changed in a way that is not compatible with projects imported by earlier plugin versions.
<br><br>
Due to internal API and project model changes in the plugin, existing project configurations cannot be safely updated through a Refresh Project operation.
<br>
Attempting to refresh may result in an incomplete project model, incorrect indexing, or build configuration issues.
<br><br>
You must re-import it from scratch using the current plugin version.
<br>
This will recreate the IntelliJ IDEA project model using the updated import logic.
<br><br>
If you encounter any issues, please submit <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/issues">new bug-report on GitHub</a> or
<br>
contact <a href="https://www.linkedin.com/in/michaellytvyn/">Mykhailo Lytvyn</a> in the project <a href="https://join.slack.com/t/sapcommercede-0kz9848/shared_invite/zt-29gnz3fd2-mz_69mla52NOFqGGsG1Zjw">Slack</a>.
""".trimIndent()
            )
                .align(Align.FILL)
        }

        separator()

        row {
            text(
                """
                    Technical references:<br>
                    - <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1699">Project Import & Refresh 3.0</a><br>
                    - <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1704">Project Import & Refresh 3.1</a>
                """.trimIndent()
            )
                .align(Align.FILL)
        }

        separator()

        row {
            link("Re-import the project...") {
                this@ProjectRefreshDialog.doCancelAction()
                invokeLater {
                    project.triggerAction("CloseProject")
                    project.triggerAction("ImportProject")
                }
            }
                .align(Align.CENTER)
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16, 32, 16)
    }

    private fun Panel.refreshScrollableUi() = row {
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

    override fun createNorthPanel() = if (canRefresh) banner(
        text = "Other settings can be found under SAP CX Settings.",
    ) else banner(
        text = "Project refresh is not supported due to major api changes",
        status = EditorNotificationPanel.Status.Error
    )

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