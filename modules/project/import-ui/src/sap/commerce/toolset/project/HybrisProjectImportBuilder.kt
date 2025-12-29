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

package sap.commerce.toolset.project

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.projectImport.ProjectImportBuilder
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectConstants.KEY_FINALIZE_PROJECT_IMPORT
import sap.commerce.toolset.project.configurator.PostImportBulkConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectImportSettings
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.tasks.ImportProjectTask
import sap.commerce.toolset.project.vfs.VirtualFileSystemService.Companion.getInstance
import java.io.File
import java.io.IOException

open class HybrisProjectImportBuilder : ProjectImportBuilder<ModuleDescriptor>() {

    private var _openProjectSettingsAfterImport = false
    private var _selectableModuleDescriptors: MutableList<ModuleDescriptor> = mutableListOf()

    var importContext: ProjectImportContext.Mutable? = null

    override val isOpenProjectSettingsAfter
        get() = _openProjectSettingsAfterImport

    override fun getName() = i18n("hybris.project.name")
    override fun getIcon() = HybrisIcons.Y.LOGO_BLUE
    override fun setOpenProjectSettingsAfter(on: Boolean) {
        _openProjectSettingsAfterImport = on
    }

    override fun createProject(name: String, path: String) = super.createProject(name, path).also {
        importContext?.project = it
    }

    override fun isMarked(element: ModuleDescriptor?): Boolean = element
        ?.isPreselected()
        ?: false

    override fun commit(
        project: Project,
        model: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?,
        artifactModel: ModifiableArtifactModel?
    ): List<Module> {
        val context = importContext
            ?.immutable(project)
            ?: return emptyList()

        val chosenModuleDescriptors = context.allChosenModuleDescriptors
            .takeIf { it.isNotEmpty() }
            ?: return emptyList()

        performProjectsCleanup(context, chosenModuleDescriptors)

        ImportProjectTask.getInstance(project).import(context)

        if (context.refresh) {
            PostImportBulkConfigurator.getInstance(project).configure(context)
        } else {
            project.putUserData(KEY_FINALIZE_PROJECT_IMPORT, context)
        }

        notifyImportNotFinishedYet(project)

        return emptyList()
    }

    override fun cleanup() = super.cleanup().also {
        importContext = null
        _openProjectSettingsAfterImport = false
    }

    override fun getList() = _selectableModuleDescriptors.toList()

    override fun setList(list: List<ModuleDescriptor>) {
        _selectableModuleDescriptors.apply {
            clear()
            addAll(list)
        }
    }

    fun initContext(importSettings: ProjectImportSettings) = ProjectImportContext.Mutable(
        rootDirectory = File(fileToImport),
        settings = importSettings,
        refresh = isUpdate,
        project = getCurrentProject(),
    ).also {
        importContext = it
    }

    // TODO: review it
    protected fun performProjectsCleanup(importContext: ProjectImportContext, chosenModuleDescriptors: Iterable<ModuleDescriptor>) {
        val alreadyExistingModuleFiles = importContext.modulesFilesDirectory
            ?.takeIf { it.isDirectory && it.exists() }
            ?.let { getAllImlFiles(it) }
            ?: chosenModuleDescriptors
                .map { it.ideaModuleFile(importContext) }
                .filter { it.exists() }

        try {
            getInstance().removeAllFiles(alreadyExistingModuleFiles)
        } catch (e: IOException) {
            thisLogger().error("Can not remove old module files.", e)
        }
    }

    private fun notifyImportNotFinishedYet(project: Project) = Notifications.create(
        type = NotificationType.INFORMATION,
        title = if (isUpdate) i18n("hybris.notification.project.refresh.title")
        else i18n("hybris.notification.project.import.title"),
        content = i18n("hybris.notification.import.or.refresh.process.not.finished.yet.content")
    )
        .notify(project)

    private fun getAllImlFiles(dir: File) = dir
        .listFiles { _, name -> name.endsWith(HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION) }
        ?.toList()
        ?: emptyList()

}
