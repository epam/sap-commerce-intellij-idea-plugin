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
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.execution.ui.*
import com.intellij.openapi.externalSystem.service.execution.configuration.addBeforeRunFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.SettingsEditorFragmentContainer
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.addSettingsEditorFragment
import com.intellij.openapi.externalSystem.service.ui.completion.TextCompletionField
import com.intellij.openapi.externalSystem.service.ui.completion.TextCompletionInfo
import com.intellij.openapi.externalSystem.service.ui.completion.TextCompletionInfoRenderer
import com.intellij.openapi.externalSystem.service.ui.util.SettingsFragmentInfo
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element


class LocalSapCXRunConfiguration(project: Project, factory: ConfigurationFactory) :
    ModuleBasedConfiguration<RunConfigurationModule, Element>(RunConfigurationModule(project), factory), TargetEnvironmentAwareRunProfile {

    override fun getValidModules(): MutableCollection<Module> = allModules

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> = LocalSapCXRunSettingsEditor(project, this)

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState = LocalSapCXRunProfileState(executor, environment, project)

    override fun readExternal(element: Element) {
        loadState(element)
    }

    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return LocalSapCXRunConfigurationOptions::class.java
    }

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean = true

    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? = null

    override fun getDefaultTargetName(): String? = null

    override fun setDefaultTargetName(targetName: String?) = Unit
}


private class LocalSapCXRunSettingsEditor(val project: Project, runConfiguration: LocalSapCXRunConfiguration) :
    FragmentedSettingsEditor<LocalSapCXRunConfiguration>(runConfiguration) {

    override fun createFragments(): List<SettingsEditorFragment<LocalSapCXRunConfiguration, *>> = SettingsEditorFragmentContainer.fragments {
        add(CommonParameterFragments.createRunHeader())
        addBeforeRunFragment(CompileStepBeforeRun.ID)
        addAll(BeforeRunFragment.createGroup())
        add(LogsGroupFragment())
        addDebugFragment()

//        addSettingsEditorFragment(
//            DebugLineInfo,
//            { CommandLineField(project, commandLineInfo, it) },
//            { it, c -> Unit },
//            { it, c -> Unit },
//        )
    }

    private fun SettingsEditorFragmentContainer<LocalSapCXRunConfiguration>.addDebugFragment() = addSettingsEditorFragment(
        DebugFragmentInfo(),
        { HostField(project) },
        { it, c -> Unit },
        { it, c -> Unit }
    )

    private class HostField(project: Project) : TextCompletionField<TextCompletionInfo>(project) {
        init {
            renderer = TextCompletionInfoRenderer()
            completionType = CompletionType.REPLACE_WORD
        }
    }

    private class DebugFragmentInfo() : SettingsFragmentInfo {
        override val settingsActionHint: String get() = "HINT?"
        override val settingsGroup: String? get() = null
        override val settingsHint: String? get() = null
        override val settingsId: String get() = "hybris.debug.settings"
        override val settingsName: String? get() = "debug"
        override val settingsPriority: Int get() = 100
        override val settingsType: SettingsEditorFragmentType get() = SettingsEditorFragmentType.COMMAND_LINE
    }
}