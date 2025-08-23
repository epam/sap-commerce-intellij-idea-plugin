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
package sap.commerce.toolset.ccv2.configurator

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ySettings

class CCv2ProjectSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "CCv2 Project Settings"

    override fun preConfigure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return

        val credentialAttributes = CredentialAttributes(CCv2Constants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)
        PasswordSafe.instance.setPassword(credentialAttributes, hybrisProjectDescriptor.ccv2Token)

        with(project.ySettings) {
            availableExtensions = buildMap {
                putAll(availableExtensions)
                CCv2Constants.CLOUD_EXTENSIONS
                    .map { ExtensionDescriptor(name = it, type = ModuleDescriptorType.CCV2_EXTERNAL) }
                    .forEach { put(it.name, it) }
            }
        }
    }
}
