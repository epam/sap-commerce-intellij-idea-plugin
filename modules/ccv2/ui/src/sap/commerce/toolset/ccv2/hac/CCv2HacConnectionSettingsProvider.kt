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

package sap.commerce.toolset.ccv2.hac

import com.intellij.openapi.project.Project
import sap.commerce.toolset.ccv2.ui.CCv2HacConnectionSettingsProviderDialog
import sap.commerce.toolset.hac.exec.HacConnectionSettingsProvider
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

class CCv2HacConnectionSettingsProvider : HacConnectionSettingsProvider {

    override val presentationText: String = "CCv2"
    override val presentationDescription: String = "Configure hAC connection settings by selecting a CCv2 Endpoint"

    override fun configure(project: Project, mutable: HacConnectionSettingsState.Mutable) {
        val providedSettings = CCv2HacConnectionSettingsProviderDialog(project).showAndRetrieve()
            ?: return

        with(mutable) {
            name.set(providedSettings.name.get())
            host.set(providedSettings.host.get())
            port.set("")
            sslProtocol.set("TLSv1.2")
            ssl.set(true)
            wsl.set(false)
        }
    }
}