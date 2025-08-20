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

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.addBeforeRunFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.addEnvironmentFragment
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.SettingsEditorFragmentContainer
import com.intellij.openapi.externalSystem.service.execution.configuration.fragments.addLabeledSettingsEditorFragment
import com.intellij.openapi.externalSystem.service.ui.util.LabeledSettingsFragmentInfo
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.bind
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.components.fields.IntegerField
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import java.io.Serial

class LocalSapCXRunSettingsEditor(val runConfiguration: LocalSapCXRunConfiguration) :
    RunConfigurationFragmentedEditor<LocalSapCXRunConfiguration>(runConfiguration, JavaRunConfigurationExtensionManager()) {

    override fun createRunFragments(): List<SettingsEditorFragment<LocalSapCXRunConfiguration, *>> = SettingsEditorFragmentContainer.fragments {
        add(CommonParameterFragments.createRunHeader())
        addBeforeRunFragment(CompileStepBeforeRun.ID)
        addAll(BeforeRunFragment.createGroup())
        addDebugHostFragment()
        addDebugPortFragment()
        addDebugLineFragment(runConfiguration.getRemoteConnection().launchCommandLine)
        add(LogsGroupFragment())
        addEnvironmentFragment()
    }

    private fun SettingsEditorFragmentContainer<LocalSapCXRunConfiguration>.addEnvironmentFragment() =
        addEnvironmentFragment(
            object : LabeledSettingsFragmentInfo {
                override val editorLabel: String = i18n("hybris.project.run.configuration.localserver.environment.variables.component.title")
                override val settingsId: String = "hybris.project.run.configuration.localserver.environment.variables.fragment"
                override val settingsName: String = i18n("hybris.project.run.configuration.localserver.environment.variables.fragment.name")
                override val settingsGroup: String = i18n("hybris.project.run.configuration.localserver.environment.options.group")
                override val settingsHint: String = i18n("hybris.project.run.configuration.localserver.environment.variables.fragment.hint")
                override val settingsActionHint: String =
                    i18n("hybris.project.run.configuration.localserver.set.custom.environment.variables.for.the.process")
            },
            { sapCXOptions.environmentProperties },
            { sapCXOptions.environmentProperties = it as MutableMap<String, String> },
            { sapCXOptions.isPassParentEnv },
            { sapCXOptions.isPassParentEnv = it },
            hideWhenEmpty = true
        )

    private fun SettingsEditorFragmentContainer<LocalSapCXRunConfiguration>.addDebugHostFragment() = addLabeledSettingsEditorFragment(
        object : LabeledSettingsFragmentInfo {
            override val editorLabel: String = i18n("hybris.project.run.configuration.localserver.host.field")

            override val settingsId: String = "hybris.localserver.debug.host.fragment"
            override val settingsName: String = "remoteDebugHost"
            override val settingsGroup: String? = null
            override val settingsHint: String = ""
            override val settingsActionHint: String? = null
        },
        { HostField(HybrisConstants.DEBUG_HOST) },
        { it, c -> c.host = it.sapCXOptions.remoteDebugHost ?: "" },
        { it, c -> it.sapCXOptions.remoteDebugHost = c.host }
    )


    private fun SettingsEditorFragmentContainer<LocalSapCXRunConfiguration>.addDebugPortFragment() = addLabeledSettingsEditorFragment(
        object : LabeledSettingsFragmentInfo {
            override val editorLabel: String = i18n("hybris.project.run.configuration.localserver.port.field")

            override val settingsId: String = "hybris.localserver.debug.port.fragment"
            override val settingsName: String = "remoteDebugPort"
            override val settingsGroup: String? = null
            override val settingsHint: String = ""
            override val settingsActionHint: String? = null
        },
        { PortField(HybrisConstants.DEBUG_PORT) },
        { it, c -> c.port = it.sapCXOptions.remoteDebugPort ?: "" },
        { it, c -> it.sapCXOptions.remoteDebugPort = c.port }
    )


    private fun SettingsEditorFragmentContainer<LocalSapCXRunConfiguration>.addDebugLineFragment(commandLine: String) = addLabeledSettingsEditorFragment(
        object : LabeledSettingsFragmentInfo {
            override val editorLabel: String = i18n("command.line.arguments.for.remote.jvm")

            override val settingsId: String = "hybris.localserver.debug.commandLine.fragment"
            override val settingsName: String = "commandLineArgument"
            override val settingsGroup: String? = null
            override val settingsHint: String = i18n("copy.and.paste.the.arguments.to.the.command.line.when.jvm.is.started")
            override val settingsActionHint: String? = null
        },
        { DebugCommandLine(commandLine) },
        { _, _ -> },
        { _, _ -> }
    )

    private class HostField(value: String) : ExtendableTextField(value) {

        private val hostProperty = AtomicProperty("")
        var host by hostProperty

        init {
            bind(hostProperty)
        }

        companion object {
            @Serial
            private const val serialVersionUID: Long = -2940721097765966640L
        }
    }

    private class PortField(myDefaultValueText: String) : IntegerField("remoteDebugPort", 1000, 65535) {

        private val portProperty = AtomicProperty("")
        var port by portProperty

        init {
            setDefaultValueText(myDefaultValueText)
            bind(portProperty)
        }

        companion object {
            @Serial
            private const val serialVersionUID: Long = -6467164709803196765L
        }
    }


    private class DebugCommandLine(commandLine: String) : ExtendableTextField(commandLine) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -6412884305856818512L
        }
    }

    private val LocalSapCXRunConfiguration.sapCXOptions: LocalSapCXRunnerOptions get() = getSapCXOptions()

}