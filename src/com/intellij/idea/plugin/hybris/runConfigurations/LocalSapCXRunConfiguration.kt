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

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.addBeforeRunFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.SettingsEditorFragmentContainer
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element


class LocalSapCXRunConfiguration(project: Project, factory: ConfigurationFactory) :
    ModuleBasedConfiguration<LocalSapCXConfigurationModule, Element>(LocalSapCXConfigurationModule(project), factory), TargetEnvironmentAwareRunProfile {

    override fun getValidModules(): MutableCollection<Module> {
        return allModules
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return LocalSapCXRunSettingsEditor(project, this)
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        return LocalSapCXRunProfileState(executor, environment, project)
    }

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean {
        return true
    }

    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? {
        return null
    }

    override fun getDefaultTargetName(): String? {
        return null
    }

    override fun setDefaultTargetName(targetName: String?) {
    }
}

private class LocalSapCXRunSettingsEditor(project: Project, runConfiguration: LocalSapCXRunConfiguration) : FragmentedSettingsEditor<LocalSapCXRunConfiguration>(runConfiguration) {

    override fun createFragments(): List<SettingsEditorFragment<LocalSapCXRunConfiguration, *>> = SettingsEditorFragmentContainer.fragments {
        add(CommonParameterFragments.createRunHeader())
        addBeforeRunFragment(CompileStepBeforeRun.ID)
        addAll(BeforeRunFragment.createGroup())
        add(LogsGroupFragment())
    }
}