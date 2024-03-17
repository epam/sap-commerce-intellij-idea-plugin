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
package com.intellij.idea.plugin.hybris.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

class HybrisCLISettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        "CCv2 CLI", "[y] SAP Commerce Cloud CLI configuration."
    ) {

        private val state = HybrisApplicationSettingsComponent.getInstance().state

        override fun createPanel() = panel {
            row {
                label("SAP CX CLI Directory:")
                    .comment(
                        """
                        SAP Commerce Cloud command line interface installation directory. Choose directory extracted from the <strong>CXCOMMCLI00P_*.zip</strong> file to enable CCv2 CLI integration.<br> 
                        All details on using SAP CCM can be found in official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/9116f1cfd16049c3a531bfb6a681ff77/8acde53272c64efb908b9f0745498015.html?locale=en-US">help.sap.com</a>.
                    """.trimIndent()
                    )
                cell(
                    textFieldWithBrowseButton(
                        null,
                        "Select SAP CX CLI Directory",
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .bindText(
                        { state.sapCLIDirectory ?: "" },
                        { state.sapCLIDirectory = it }
                    )
                    .validationOnInput {
                        val executable = if (SystemInfo.isWindows) "sapccm.bat"
                        else "sapccm"

                        val valid = Paths.get(it.text, "bin", executable)
                            .takeIf { path -> path.exists() }
                            ?.isExecutable()
                            ?: false
                        if (!valid) {
                            this.error("Invalid SAP CCM directory, cannot find <strong>bin/$executable</strong> executable file.")
                        } else {
                            null
                        }
                    }
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
    }
}
