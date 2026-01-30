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

import com.intellij.ide.util.ElementsChooser
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.table.JBTable
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.settings.LibrarySourcesFetchMode
import sap.commerce.toolset.ui.banner
import java.awt.Dimension
import javax.swing.Icon
import javax.swing.ScrollPaneConstants

class ProjectRefreshDialog(
    private val project: Project,
    private val refreshContext: ProjectRefreshContext.Mutable,
) : DialogWrapper(project) {

    private val orderByType = mapOf(
        ModuleDescriptorType.CONFIG to 0,
        ModuleDescriptorType.NONE to 0,
        ModuleDescriptorType.CUSTOM to 1,
        ModuleDescriptorType.OOTB to 2,
        ModuleDescriptorType.PLATFORM to 3,
        ModuleDescriptorType.EXT to 4,
    )

    private val elementsChooser = ClearableLazyValue.create { additionalExtensions() }

    private val ui = ClearableLazyValue.create {
        panel {
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
                    comboBox(
                        model = EnumComboBoxModel(LibrarySourcesFetchMode::class.java),
                        renderer = SimpleListCellRenderer.create("?") { it.presentationText }
                    )
                        .bindItem(refreshContext.importSettings.librarySourcesFetchMode)

                    label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                    checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                        .bindSelected(refreshContext.importSettings.withExternalLibrarySources)

                    checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                        .bindSelected(refreshContext.importSettings.withExternalLibraryJavadocs)

                    contextHelp(i18n("hybris.project.import.withExternalLibrarySources.help.description"))
                }

                row {
                    checkBox(i18n("hybris.project.import.withDecompiledOotbSources"))
                        .bindSelected(refreshContext.importSettings.withDecompiledOotbSources)
                    contextHelp(i18n("hybris.project.import.withDecompiledOotbSources.help.description"))
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

            collapsibleGroup("Additional Extensions (Unused)") {
                row {
                    cell(elementsChooser.value)
                        .onApply {
                            val newAdditionalElements = elementsChooser.value.markedElements
                                .map { it.name }
                            refreshContext.importSettings.unusedExtensions.set(newAdditionalElements)
                        }
                        .align(Align.FILL)
                }
            }
        }.apply {
            border = JBUI.Borders.empty(8, 16, 32, 16)
            registerValidators(disposable)
        }
    }

    init {
        title = "Refresh the Project"
        isResizable = false
        super.init()
    }

    override fun createCenterPanel() = panel {
        row {
            scrollCell(ui.value)
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
        .apply {
            preferredSize = Dimension(JBUIScale.scale(600), JBUIScale.scale(400))
        }

    override fun createNorthPanel() = banner(
        text = "Other settings can be found under SAP CX Settings.",
    )

    override fun getStyle(): DialogStyle = DialogStyle.COMPACT
    override fun doValidateAll() = ui.value.validateAll()
        .filter { it.component?.isVisible ?: false }
        .onEach { it.okEnabled = true }

    override fun applyFields() {
        ui.value.apply()
    }

    override fun dispose() {
        super.dispose()
        elementsChooser.drop()
        ui.drop()
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

    private fun additionalExtensions(): ElementsChooser<ExtensionDescriptor> {
        val chooser = ExtensionElementsChooser()
        val settings = project.ySettings

        val module2extensionMapping = settings.module2extensionMapping.entries
            .associate { it.value to it.key }
        val selectableExtensions = settings.extensionDescriptors
            .filterNot { settings.modulesOnBlackList.contains(it.name) }
            .filter { settings.unusedExtensions.contains(it.name) || !module2extensionMapping.containsKey(it.name) }
        val sortedExtensions = selectableExtensions
            .sortedWith(
                compareBy<ExtensionDescriptor> {
                    !settings.unusedExtensions.contains(it.name)
                }
                    .thenComparing { orderByType[it.type] ?: Integer.MAX_VALUE }
                    .thenComparing { it.name }
            )

        sortedExtensions.forEach {
            chooser.addElement(it, settings.unusedExtensions.contains(it.name))
        }
        return chooser.also {
            it.component
                ?.asSafely<JBTable>()
                ?.changeSelection(0, 0, false, false)
        }
    }
}
