/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element


class LocalSapCXRunConfiguration(project: Project, factory: ConfigurationFactory) :
    ModuleBasedConfiguration<RunConfigurationModule, Element>(RunConfigurationModule(project), factory), TargetEnvironmentAwareRunProfile {

    override fun getValidModules(): MutableCollection<Module> = allModules

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> = LocalSapCXRunSettingsEditor(this)

    override fun getOptionsClass(): Class<out RunConfigurationOptions> = LocalSapCXRunnerOptions::class.java


    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState = LocalSapCXRunProfileState(executor, environment, project, this)

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean = true

    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? = null

    override fun getDefaultTargetName(): String? = null

    override fun setDefaultTargetName(targetName: String?) = Unit

    fun getSapCXOptions(): LocalSapCXRunnerOptions = super.getOptions() as LocalSapCXRunnerOptions

    fun getRemoteConnetion(): RemoteConnection = RemoteConnection(true, getSapCXOptions().remoteDebugHost, getSapCXOptions().remoteDebugPort, false)
}