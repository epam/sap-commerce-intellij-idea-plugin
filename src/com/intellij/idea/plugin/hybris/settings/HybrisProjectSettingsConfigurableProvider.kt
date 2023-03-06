/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.forms.HybrisProjectSettingsForm
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

class HybrisProjectSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = HybrisProjectSettingsConfigurable(project)

    class HybrisProjectSettingsConfigurable(private val project: Project) : Configurable, Disposable {
        private val settingsForm = HybrisProjectSettingsForm()

        init {
            Disposer.register(this, settingsForm)
        }

        override fun getDisplayName() = message("hybris.settings.project.title")

        override fun createComponent(): JComponent {
            return settingsForm
                .init(project)
                .mainPanel
        }

        override fun isModified() = settingsForm.isModified(project)
        override fun apply() = settingsForm.apply(project)
        override fun reset() {
            settingsForm.setData(project)
        }

        override fun disposeUIResources() {
            Disposer.dispose(settingsForm)
        }

        override fun dispose() {
            // NOP
        }
    }
}
