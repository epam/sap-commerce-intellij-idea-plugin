/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.util.IconLoader
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.project.HybrisProjectImportBuilder
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.ui.italic
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.awt.Dimension
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.listDirectoryEntries

class ReuseExistingProjectSettings(context: WizardContext) : ProjectImportWizardStep(context) {

    private var _ui: JComponent? = null
    private val graph = PropertyGraph()
    private val projectNameProperty = AtomicProperty<String?>(null)
    private val checkboxProperties = mutableMapOf<String, RestoreOption>()

    override fun updateStep() {
        checkboxProperties.clear()
        _ui = panel {
            val ideaPath = Path(builder.fileToImport)
                .resolve(ProjectConstants.Paths.IDEA)
            val ideaDirectory = ideaPath.absolutePathString()

            row {
                text(
                    """
                        Reusable configuration from the existing project has been detected.<br>
                        The selected items below will be preserved and reused during import. You can adjust the selection before proceeding.
                    """.trimIndent()
                ).align(AlignX.FILL)
            }

            row {
                text(ideaDirectory)
                    .label("Project settings (.idea):")
            }

            row {
                button("Select All") {
                    checkboxProperties.values.forEach { it.property.set(true) }
                }

                button("Deselect All") {
                    checkboxProperties.values.forEach { it.property.set(false) }
                }
            }

            separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)

            checkBoxFile(ideaPath, ".name", "Project name") { path ->
                runCatching {
                    Files.readString(path, StandardCharsets.UTF_8)
                }.onSuccess { projectName ->
                    projectNameProperty.set(projectName)
                    label(projectName).italic()
                }
            }
            checkBoxFile(ideaPath, "icon.svg", "Project icon (light theme)") { path ->
                IconLoader.findIcon(path.toUri().toURL(), false)
                    ?.let { IconUtil.scale(it, null, 16f / it.iconWidth) }
                    ?.let { icon(it) }
            }
            checkBoxFile(ideaPath, "icon_dark.svg", "Project icon (dark theme)") { path ->
                IconLoader.findIcon(path.toUri().toURL(), false)
                    ?.let { IconUtil.scale(it, null, 16f / it.iconWidth) }
                    ?.let { icon(it) }
            }
            checkBoxFile(ideaPath, "hybrisDeveloperSpecificProjectSettings.xml", "Plugin developer specific project settings")
            checkBoxFile(ideaPath, "vcs.xml", "Version control systems")
            checkBoxFile(ideaPath, "externalDependencies.xml", "External dependencies on other plugins")

            checkBoxDirectory(ideaPath, ".run", "Run configurations (modern format)")
            checkBoxDirectory(ideaPath, "runConfigurations", "Run configurations (legacy format)")
            checkBoxDirectory(ideaPath, "dictionaries", "Dictionaries")
            checkBoxDirectory(ideaPath, "copyright", "Copyright profiles")
            checkBoxDirectory(ideaPath, "codeStyles", "Project code styles")
        }
    }

    override fun getComponent() = JBScrollPane(_ui ?: JPanel()).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(preferredSize.width, JBUIScale.scale(600))
    }

    override fun updateDataModel() {
        val mutableContext = importContext ?: return
        mutableContext.projectName = projectNameProperty.get()
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { checkboxProperties[".name"]?.property?.get() ?: false }
        mutableContext.restoreExistingProjectFiles = checkboxProperties.values
            .filter { it.property.get() }
            .map { it.path }
    }

    override fun isStepVisible(): Boolean {
        val importContext = importContext ?: return false
        if (importContext.refresh) return false
        val ideaPath = Path(builder.fileToImport)
            .resolve(ProjectConstants.Paths.IDEA)

        val anyExists = arrayOf(
            ".run",
            "runConfigurations",
            "dictionaries",
            "copyright",
            "codeStyles",
        )
            .map { ideaPath.resolve(it) }
            .firstOrNull { it.directoryExists }
            ?: arrayOf(
                ".name",
                "icon.svg",
                "icon_dark.svg",
                "hybrisDeveloperSpecificProjectSettings.xml",
                "vcs.xml",
                "externalDependencies.xml",
            )
                .map { ideaPath.resolve(it) }
                .firstOrNull { it.fileExists }

        return anyExists != null
    }

    private fun Panel.checkBoxFile(ideaPath: Path, childName: String, checkBoxText: String, preview: Row.(Path) -> Unit = {}) = checkbox(
        ideaPath = ideaPath,
        childName = childName,
        textProvider = { checkBoxText },
        preview = preview
    ) { fileExists }

    private fun Panel.checkBoxDirectory(ideaPath: Path, childName: String, checkBoxText: String, preview: Row.(Path) -> Unit = {}) = checkbox(
        ideaPath = ideaPath,
        childName = childName,
        textProvider = {
            runCatching { this.listDirectoryEntries().size }
                .getOrNull()
                ?.let { entriesSize -> "$checkBoxText | found $entriesSize entries" }
                ?: checkBoxText
        },
        preview = preview
    ) { directoryExists }

    private fun Panel.checkbox(ideaPath: Path, childName: String, textProvider: Path.() -> String, preview: Row.(Path) -> Unit, takeIf: Path.() -> Boolean): Row? =
        ideaPath.resolve(childName)
            .takeIf(takeIf)
            ?.let { path ->
                row {
                    val property = graph.property(false)
                    checkboxProperties[childName] = RestoreOption(
                        name = childName,
                        property = property,
                        path = path,
                    )
                    checkBox(textProvider(path))
                        .bindSelected(property)
                        .comment(".idea/$childName")
                    preview(path)
                }
            }

    private val importContext: ProjectImportContext.Mutable?
        get() = builder.asSafely<HybrisProjectImportBuilder>()
            ?.context

    private data class RestoreOption(
        val name: String,
        val property: GraphProperty<Boolean>,
        val path: Path,
    )
}
