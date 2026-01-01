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
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.selected
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.equalsIgnoreOrder
import sap.commerce.toolset.i18n
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.ui.CRUDListPanel
import javax.swing.Icon
import javax.swing.JCheckBox
import kotlin.reflect.KMutableProperty0

class ApplicationSettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "[y] SAP Commerce", "[y] SAP CX configuration."
    ) {
        private val applicationSettings = ApplicationSettings.getInstance()
        private lateinit var groupModulesCheckBox: JCheckBox
        private lateinit var externalModulesCheckBox: JCheckBox

        private val junkList = CRUDListPanel(
            "hybris.import.settings.junk.directory.popup.add.title",
            "hybris.import.settings.junk.directory.popup.add.text",
            "hybris.import.settings.junk.directory.popup.edit.title",
            "hybris.import.settings.junk.directory.popup.edit.text",
        )
        private val excludeResources = CRUDListPanel(
            "hybris.import.settings.exclude.resources.popup.add.title",
            "hybris.import.settings.exclude.resources.popup.add.text",
            "hybris.import.settings.exclude.resources.popup.edit.title",
            "hybris.import.settings.exclude.resources.popup.edit.text",
        )
        private val excludeFromIndex = CRUDListPanel(
            "hybris.import.settings.excludedFromIndex.directory.popup.add.title",
            "hybris.import.settings.excludedFromIndex.directory.popup.add.text",
            "hybris.import.settings.excludedFromIndex.directory.popup.edit.title",
            "hybris.import.settings.excludedFromIndex.directory.popup.edit.text",
        )

        private val _ui = panel {
            group(i18n("hybris.project.import.projectImportSettings.title")) {
                row {
                    checkBox(i18n("hybris.project.import.followSymlink"))
                        .bindSelected(applicationSettings::followSymlink)
                    contextHelp(i18n("hybris.project.import.followSymlink.help.description"))
                }

                row {
                    checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                        .bindSelected(applicationSettings::defaultPlatformInReadOnly)
                    contextHelp(i18n("hybris.import.wizard.import.ootb.modules.read.only.help.description"))
                }

                row {
                    checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                        .bindSelected(applicationSettings::useFakeOutputPathForCustomExtensions)
                    contextHelp(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.help.description"))
                }

                row {
                    checkBox(i18n("hybris.project.import.excludeTestSources"))
                        .bindSelected(applicationSettings::excludeTestSources)
                }

                row {
                    checkBox(i18n("hybris.project.import.ignore.non.existing.sources"))
                        .bindSelected(applicationSettings::ignoreNonExistingSourceDirectories)
                }

                row {
                    checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                        .bindSelected(applicationSettings::importCustomAntBuildFiles)
                    contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.help.description"))
                }

                row {
                    checkBox(i18n("hybris.project.import.withStandardProvidedSources"))
                        .bindSelected(applicationSettings::withStandardProvidedSources)
                    contextHelp(i18n("hybris.project.import.withStandardProvidedSources.help.description"))
                }

                row {
                    label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                    checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                        .bindSelected(applicationSettings::withExternalLibrarySources)

                    checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                        .bindSelected(applicationSettings::withExternalLibraryJavadocs)

                    contextHelp(i18n("hybris.project.import.withExternalLibrarySources.help.description"))
                }
            }

            group(i18n("hybris.project.import.projectStructure.title")) {
                row {
                    checkBox(i18n("hybris.project.view.tree.hide.empty.middle.folders"))
                        .bindSelected(applicationSettings::hideEmptyMiddleFolders)
                }

                row {
                    groupModulesCheckBox = checkBox(i18n("hybris.import.settings.group.modules"))
                        .bindSelected(applicationSettings::groupModules)
                        .component
                }

                indent {
                    groupProperties(
                        HybrisIcons.Extension.CUSTOM,
                        i18n("hybris.import.settings.group.custom"),
                        i18n("hybris.import.settings.group.unused"),
                        applicationSettings::groupCustom,
                        applicationSettings::groupOtherCustom,
                        groupModulesCheckBox.selected
                    )
                    groupProperties(
                        HybrisIcons.Extension.OOTB,
                        i18n("hybris.import.settings.group.hybris"),
                        i18n("hybris.import.settings.group.unused"),
                        applicationSettings::groupHybris,
                        applicationSettings::groupOtherHybris,
                        groupModulesCheckBox.selected
                    )
                    groupProperties(
                        HybrisIcons.Extension.PLATFORM,
                        i18n("hybris.import.settings.group.platform"),
                        i18n("hybris.import.settings.group.nonhybris"),
                        applicationSettings::groupPlatform,
                        applicationSettings::groupNonHybris,
                        groupModulesCheckBox.selected
                    )

                    row {
                        icon(HybrisIcons.Module.CCV2_GROUP)
                        groupProperty(
                            i18n("hybris.import.settings.group.ccv2"),
                            applicationSettings::groupCCv2,
                            groupModulesCheckBox.selected
                        )
                    }.layout(RowLayout.PARENT_GRID)
                }.visibleIf(groupModulesCheckBox.selected)

                row {
                    externalModulesCheckBox = checkBox("Group external modules")
                        .bindSelected(applicationSettings::groupExternalModules)
                        .component
                    contextHelp(i18n("hybris.project.view.external.module.help.description"))
                }

                indent {
                    row {
                        icon(HybrisIcons.Module.EXTERNAL_GROUP)
                        groupProperty(
                            i18n("hybris.import.settings.group.externalModules"),
                            applicationSettings::groupNameExternalModules,
                            externalModulesCheckBox.selected
                        )
                    }.layout(RowLayout.PARENT_GRID)
                }.visibleIf(externalModulesCheckBox.selected)
            }

            crudList(
                i18n("hybris.import.settings.junk.directory.name"),
                junkList,
                applicationSettings::junkDirectoryList,
                i18n("hybris.import.settings.junk.directory.help")
            )

            crudList(
                i18n("hybris.import.settings.exclude.resources.name"),
                excludeResources,
                applicationSettings::extensionsResourcesToExclude,
                "Use SAP Commerce extension name, not fully qualified IDEA module name."
            )

            crudList(
                i18n("hybris.import.settings.excludedFromIndex.directory.name"),
                excludeFromIndex,
                applicationSettings::excludedFromIndexList
            )
        }

        override fun apply() {
            if (_ui.validateAll().none { it.component?.isVisible ?: false }) {
                super.apply()
            }
        }

        private fun Panel.crudList(
            groupTitle: String,
            listPanel: CRUDListPanel,
            property: KMutableProperty0<List<String>>,
            commentText: String? = null,
        ) {
            group(groupTitle, false) {
                if (commentText != null) {
                    row {
                        comment(commentText)
                    }
                }
                row {
                    cell(listPanel)
                        .align(AlignX.FILL)
                        .onApply { property.set(listPanel.data) }
                        .onReset { listPanel.data = property.get() }
                        .onIsModified { listPanel.data.equalsIgnoreOrder(property.get()).not() }
                }
            }
        }

        private fun Panel.groupProperties(
            icon: Icon,
            groupLabel: String,
            groupOtherLabel: String,
            groupProperty: KMutableProperty0<String>,
            groupOtherProperty: KMutableProperty0<String>,
            enabledIf: ComponentPredicate
        ) {
            row {
                icon(icon)
                groupProperty(groupLabel, groupProperty, enabledIf)
                groupProperty(groupOtherLabel, groupOtherProperty, enabledIf)
            }.layout(RowLayout.PARENT_GRID)
        }

        private fun Row.groupProperty(
            groupLabel: String,
            groupProperty: KMutableProperty0<String>,
            enabledIf: ComponentPredicate
        ) {
            label(groupLabel)
            textField()
                .bindText(groupProperty)
                .addValidationRule(i18n("hybris.settings.validations.notBlank")) { it.text.isBlank() }
                .enabledIf(enabledIf)
                .applyIfEnabled()
        }

        override fun createPanel() = _ui
    }
}