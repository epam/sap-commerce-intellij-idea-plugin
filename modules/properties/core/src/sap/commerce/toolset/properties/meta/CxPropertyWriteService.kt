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

package sap.commerce.toolset.properties.meta

import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * Writes declarations into the project's own property files.
 *
 * Targets are taken from the collected chain rather than from the file system, so a file only shows up once it is part
 * of the configuration the running system would read.
 */
@Service(Service.Level.PROJECT)
class CxPropertyWriteService(private val project: Project) {

    /**
     * Files a declaration may be added to, `local.properties` first and the custom extensions after it, by name.
     * Must be called under a read action.
     */
    fun targets(): List<CxPropertyTarget> {
        val customExtensions = project.ySettings.extensionDescriptors
            .filter { it.type == ModuleDescriptorType.CUSTOM }
            .mapTo(HashSet(), ExtensionDescriptor::name)

        return CxPropertyCollector.getInstance(project).collect().sources
            .mapNotNull { source ->
                val file = source.file ?: return@mapNotNull null

                when {
                    source.scope == CxPropertyScope.LOCAL -> CxPropertyTarget(file, source.scope)
                    source.scope == CxPropertyScope.PROJECT && source.extension in customExtensions ->
                        CxPropertyTarget(file, source.scope, source.extension)

                    else -> null
                }
            }
            .distinct()
            .sortedWith(compareBy({ it.scope != CxPropertyScope.LOCAL }, { it.presentableName }))
    }

    /**
     * Declares [properties] in [target], replacing the value of the ones it already declares.
     *
     * @return the properties actually written, or an empty list when the target turned out not to be a properties file.
     */
    suspend fun write(target: CxPropertyTarget, properties: Collection<CxPropertyPresentation>): List<CxPropertyPresentation> {
        if (properties.isEmpty()) return emptyList()

        val propertiesFile = readAction {
            PsiManager.getInstance(project)
                .findFile(target.file)
                ?.asSafely<PropertiesFile>()
        } ?: return emptyList()

        withContext(Dispatchers.EDT) {
            WriteCommandAction.runWriteCommandAction(project, COMMAND_NAME, null, {
                properties.forEach { property ->
                    propertiesFile.findPropertyByKey(property.key)
                        ?.setValue(property.value)
                        ?: propertiesFile.addProperty(property.key, property.value)
                }
            }, propertiesFile.containingFile)
        }

        CxPropertyCollector.getInstance(project).resetCache()

        return properties.toList()
    }

    companion object {
        private const val COMMAND_NAME = "Declare SAP Commerce Properties"

        fun getInstance(project: Project): CxPropertyWriteService = project.service()
    }
}
