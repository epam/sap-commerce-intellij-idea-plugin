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

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import org.apache.commons.lang3.SystemUtils
import org.jdom.Element
import java.nio.file.Paths
import javax.swing.JComponent
import javax.swing.JPanel


class LocalSapCXRunConfiguration(project: Project, factory: ConfigurationFactory) :
    ModuleBasedConfiguration<LocalSapCXConfigurationModule, Element>(LocalSapCXConfigurationModule(project), factory), TargetEnvironmentAwareRunProfile {

    override fun getValidModules(): MutableCollection<Module> {
        return allModules
    }

    fun getScriptPath(): String {
        val basePath = project.basePath!!
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val hybrisDirectory = settings.hybrisDirectory!!
        val script = if (SystemUtils.IS_OS_WINDOWS) HybrisConstants.HYBRIS_SERVER_BASH_SCRIPT_NAME else HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME

        return Paths.get(basePath, hybrisDirectory, script).toString()
    }

    fun getWorkDirectory(): String {
        val basePath = project.basePath!!
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val hybrisDirectory = settings.hybrisDirectory!!

        return Paths.get(basePath, hybrisDirectory, HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return LocalSapCXRunSettingsEditor()
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        TODO("Change to TargetEnvironmentAwareRunProfileState")
        return object : CommandLineState(environment) {
            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine(getScriptPath())
                commandLine.setWorkDirectory(getWorkDirectory())
                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? {
        TODO("Not yet implemented")
    }

    override fun getDefaultTargetName(): String? {
        TODO("Not yet implemented")
    }

    override fun setDefaultTargetName(targetName: String?) {
        TODO("Not yet implemented")
    }

}

private class LocalSapCXRunSettingsEditor : SettingsEditor<LocalSapCXRunConfiguration>() {
    private val myPanel: JPanel
    private val scriptPathField = TextFieldWithBrowseButton()

    init {
        scriptPathField.addBrowseFolderListener(
            "Select Script File", null, null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
        myPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Script file", scriptPathField)
            .getPanel()
    }

    override fun resetEditorFrom(runConfiguration: LocalSapCXRunConfiguration) {
        scriptPathField.text = runConfiguration.getScriptPath()
    }

    override fun applyEditorTo(runConfiguration: LocalSapCXRunConfiguration) {
//        runConfiguration.setScriptName(scriptPathField.text)
    }

    override fun createEditor(): JComponent {
        return myPanel
    }
}