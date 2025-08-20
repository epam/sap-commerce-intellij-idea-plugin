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

package sap.commerce.toolset.project.runConfigurations

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RemoteConnection
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import org.jdom.Element

class LocalSapCXRunConfiguration(project: Project, factory: ConfigurationFactory) :
    ModuleBasedConfiguration<RunConfigurationModule, Element>(RunConfigurationModule(project), factory), TargetEnvironmentAwareRunProfile {

    override fun getValidModules(): MutableCollection<Module> = allModules
    override fun getConfigurationEditor() = LocalSapCXRunSettingsEditor(this)
    override fun getOptionsClass() = LocalSapCXRunnerOptions::class.java
    override fun getState(executor: Executor, environment: ExecutionEnvironment) = LocalSapCXRunProfileState(executor, environment, project, this)

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean = true
    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? = null
    override fun getDefaultTargetName(): String? = null
    override fun setDefaultTargetName(targetName: String?) = Unit

    fun getSapCXOptions() = super.getOptions() as LocalSapCXRunnerOptions
    fun getRemoteConnection() = RemoteConnection(true, getSapCXOptions().remoteDebugHost, getSapCXOptions().remoteDebugPort, false)

    companion object {
        private const val serialVersionUID: Long = 3578963582172536206L
    }
}