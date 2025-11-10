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

package sap.commerce.toolset.hac.auth

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.ui.HacManualAuthenticationDialog

class HacManualAuthenticatorImpl(private val project: Project) : HacManualAuthenticator {

    override suspend fun authenticate(settings: HacConnectionSettingsState): HacAuthContext? {
        val deferredAuthenticationContext = CompletableDeferred<HacAuthContext?>()

        val proxyCredentials = withContext(Dispatchers.IO) {
            HacExecConnectionService.getInstance(project).getProxyCredentials(settings)
        }

        withContext(Dispatchers.EDT) {
            HacManualAuthenticationDialog(project, settings, proxyCredentials, deferredAuthenticationContext).show()
        }
        return deferredAuthenticationContext.await()
    }
}