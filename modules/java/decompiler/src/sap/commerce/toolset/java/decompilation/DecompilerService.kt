/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.java.decompilation

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.LegalNoticeDialog
import com.intellij.util.application
import org.jetbrains.java.decompiler.IdeaDecompilerBundle
import sap.commerce.toolset.Plugin

/**
 * Service to handle IdeaDecompiler legal notice consent.
 */
@Service(Service.Level.APP)
class DecompilerService {

    private val consentKey = "decompiler.legal.notice.accepted"

    fun isConsentGranted(): Boolean = PropertiesComponent.getInstance().isValueSet(consentKey)

    fun ensureConsentAccepted(): ConsentResult {
        val properties = PropertiesComponent.getInstance()
        if (properties.isValueSet(consentKey)) return ConsentResult.Accepted

        val title = IdeaDecompilerBundle.message("legal.notice.title", "sap-commerce-ootb")
        val message = IdeaDecompilerBundle.message("legal.notice.text")
        val result = LegalNoticeDialog.build(title, message)
            .withCancelText(IdeaDecompilerBundle.message("legal.notice.action.postpone"))
            .withCustomAction(IdeaDecompilerBundle.message("legal.notice.action.reject"), DialogWrapper.NEXT_USER_EXIT_CODE)
            .show()

        return when (result) {
            DialogWrapper.OK_EXIT_CODE -> {
                properties.setValue(consentKey, true)
                ConsentResult.Accepted
            }

            DialogWrapper.NEXT_USER_EXIT_CODE -> {
                disableDecompilerPlugin()
                ConsentResult.Rejected
            }

            else -> ConsentResult.Postponed
        }
    }

    private fun disableDecompilerPlugin() {
        PluginManagerCore.disablePlugin(Plugin.JAVA_DECOMPILER.pluginId)
    }

    enum class ConsentResult {
        Accepted,
        Postponed,
        Rejected,
    }

    companion object {
        fun getInstance(): DecompilerService = application.service()
    }
}
